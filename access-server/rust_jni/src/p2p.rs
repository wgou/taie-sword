// Android WebRTC 客户端库
// 包含WebSocket信令 + WebRTC P2P连接
use std::sync::Arc;

// 异步运行时和网络
use tokio::sync::{ mpsc, Mutex };
use tokio_tungstenite::{ connect_async, tungstenite::Message };
use futures_util::{ SinkExt, StreamExt };

// WebRTC
use webrtc::api::interceptor_registry::register_default_interceptors;
use webrtc::api::media_engine::MediaEngine;
use webrtc::api::APIBuilder;
use webrtc::data_channel::RTCDataChannel;
use webrtc::data_channel::data_channel_state::RTCDataChannelState;
use webrtc::ice_transport::ice_server::RTCIceServer;
use webrtc::peer_connection::configuration::RTCConfiguration;
use webrtc::peer_connection::peer_connection_state::RTCPeerConnectionState;
use webrtc::peer_connection::sdp::session_description::RTCSessionDescription;
use webrtc::peer_connection::RTCPeerConnection;
use webrtc::Error as WebRTCError;

// JSON序列化
use serde::{ Deserialize, Serialize };

// 日志
use log::{ info, debug, error, warn };

// 字节处理 - 通过 webrtc 重新导出
use webrtc::data_channel::data_channel_message::DataChannelMessage;

// Android 回调接口定义
pub trait WebRTCCallback: Send + Sync {
    fn on_connected(&self, client_id: String);
    fn on_user_joined(&self, user_id: String);
    fn on_user_left(&self, user_id: String);
    fn on_message_received(&self, sender_id: String, message: String, is_p2p: bool);
    fn on_binary_data_received(&self, sender_id: String, data: Vec<u8>, is_p2p: bool);
    fn on_connection_state_changed(&self, state: String);
    fn on_error(&self, error: String);
}

// WebRTC 客户端配置
#[derive(Debug, Clone)]
pub struct WebRTCConfig {
    pub room_id: String,
    pub server_url: String,
    pub debug: bool,
    pub stun_servers: Vec<String>,
}

// 定义信令消息枚举，与服务器保持一致
#[derive(Serialize, Deserialize, Debug, Clone)]
#[serde(tag = "type")]
enum SignalMessage {
    #[serde(rename = "join")] Join {
        room_id: String,
    },

    #[serde(rename = "offer")] Offer {
        room_id: String,
        sdp: String,
    },

    #[serde(rename = "answer")] Answer {
        room_id: String,
        sdp: String,
    },

    #[serde(rename = "ice-candidate")] IceCandidate {
        room_id: String,
        candidate: String,
    },

    #[serde(rename = "chat-message")] ChatMessage {
        room_id: String,
        message: String,
        sender_id: String,
    },

    #[serde(rename = "joined")] Joined {
        client_id: String,
    },

    #[serde(rename = "user-joined")] UserJoined {
        client_id: String,
    },

    #[serde(rename = "user-left")] UserLeft {
        client_id: String,
    },

    #[serde(rename = "error")] Error {
        message: String,
    },
}

// WebRTC连接状态
#[derive(Debug, Clone)]
enum ConnectionMode {
    ServerForwarded, // 服务器转发模式
    P2PConnecting, // P2P连接中
    P2PConnected, // P2P已连接
}

// WebRTC客户端结构体 - Android 版本
pub struct AndroidWebRTCClient {
    config: WebRTCConfig,
    client_id: Option<String>,
    callback: Option<Arc<dyn WebRTCCallback>>,

    // WebRTC相关
    peer_connection: Option<Arc<RTCPeerConnection>>,
    data_channel: Option<Arc<RTCDataChannel>>,
    connection_mode: ConnectionMode,

    // 信令状态追踪
    is_initiator: bool, // 是否为连接发起方
    has_sent_offer: bool, // 是否已发送offer
    has_received_offer: bool, // 是否已收到offer

    // 通信通道
    ws_sender: Option<
        Arc<
            Mutex<
                futures_util::stream::SplitSink<
                    tokio_tungstenite::WebSocketStream<tokio_tungstenite::MaybeTlsStream<tokio::net::TcpStream>>,
                    Message
                >
            >
        >
    >,

    // 任务管理
    task_handles: Vec<tokio::task::JoinHandle<()>>,
    shutdown_tx: Option<mpsc::UnboundedSender<()>>,
}

impl AndroidWebRTCClient {
    // 创建新的WebRTC客户端实例
    pub fn new(config: WebRTCConfig) -> Self {
        Self {
            config,
            client_id: None,
            callback: None,
            peer_connection: None,
            data_channel: None,
            connection_mode: ConnectionMode::ServerForwarded,
            is_initiator: false,
            has_sent_offer: false,
            has_received_offer: false,
            ws_sender: None,
            task_handles: Vec::new(),
            shutdown_tx: None,
        }
    }

    // 设置回调
    pub fn set_callback(&mut self, callback: Arc<dyn WebRTCCallback>) {
        self.callback = Some(callback);
    }

    // 调试日志
    fn debug_log(&self, message: &str) {
        if self.config.debug {
            debug!("{}", message);
        }
    }

    // 信息日志
    fn info_log(&self, message: &str) {
        info!("{}", message);
    }

    // 错误日志
    fn error_log(&self, message: &str) {
        error!("{}", message);
    }

    // 通知回调
    fn notify_callback<F>(&self, f: F) where F: FnOnce(&dyn WebRTCCallback) {
        if let Some(ref callback) = self.callback {
            f(callback.as_ref());
        }
    }

    // 状态日志
    fn log_signaling_state(&self, action: &str) {
        if self.config.debug {
            let state = format!(
                "{} - 发起方:{} 已发送offer:{} 已收到offer:{}",
                action,
                self.is_initiator,
                self.has_sent_offer,
                self.has_received_offer
            );
            self.debug_log(&state);
        }
    }

    // 显示当前连接模式
    fn show_connection_mode(&self) {
        let mode_str = match self.connection_mode {
            ConnectionMode::ServerForwarded => "服务器转发模式",
            ConnectionMode::P2PConnecting => "P2P连接中",
            ConnectionMode::P2PConnected => "P2P直连模式",
        };
        self.info_log(&format!("连接模式: {}", mode_str));
        self.notify_callback(|cb| cb.on_connection_state_changed(mode_str.to_string()));
    }

    // 检查是否可以处理信令消息
    fn can_handle_offer(&self) -> bool {
        // 如果还没有peer connection，或者已经有offer但peer connection在stable状态
        self.peer_connection.is_none() || !self.has_received_offer
    }

    fn can_handle_answer(&self) -> bool {
        // 只有发送了offer且还没收到answer时，才能处理answer
        self.has_sent_offer && self.peer_connection.is_some()
    }

    // 重置连接状态
    fn reset_connection_state(&mut self) {
        self.peer_connection = None;
        self.data_channel = None;
        self.connection_mode = ConnectionMode::ServerForwarded;
        self.is_initiator = false;
        self.has_sent_offer = false;
        self.has_received_offer = false;
        self.show_connection_mode();
    }

    // 创建WebRTC API
    async fn create_webrtc_api() -> Result<webrtc::api::API, WebRTCError> {
        let mut media_engine = MediaEngine::default();
        media_engine.register_default_codecs()?;

        let mut registry = webrtc::interceptor::registry::Registry::new();
        registry = register_default_interceptors(registry, &mut media_engine)?;

        let api = APIBuilder::new()
            .with_media_engine(media_engine)
            .with_interceptor_registry(registry)
            .build();

        Ok(api)
    }

    // 连接到服务器并启动完整的WebRTC会话
    pub async fn connect_and_start(
        client_arc: Arc<Mutex<AndroidWebRTCClient>>
    ) -> Result<(), Box<dyn std::error::Error>> {
        let (server_url, room_id) = {
            let client = client_arc.lock().await;
            (client.config.server_url.clone(), client.config.room_id.clone())
        };

        info!("连接到信令服务器: {}", server_url);

        // 连接WebSocket信令服务器
        let (ws_stream, _) = connect_async(&server_url).await?;
        info!("已连接到信令服务器");

        let (ws_sender, mut ws_receiver) = ws_stream.split();
        let ws_sender = Arc::new(Mutex::new(ws_sender));

        // 创建shutdown通道
        let (shutdown_tx, mut shutdown_rx) = mpsc::unbounded_channel();

        // 设置WebSocket发送器和shutdown通道
        {
            let mut client = client_arc.lock().await;
            client.ws_sender = Some(Arc::clone(&ws_sender));
            client.shutdown_tx = Some(shutdown_tx);
        }

        // 发送加入房间消息
        let join_msg = SignalMessage::Join {
            room_id: room_id.clone(),
        };
        if let Ok(json) = serde_json::to_string(&join_msg) {
            let mut sender = ws_sender.lock().await;
            let _ = sender.send(Message::Text(json)).await;
        }

        // 创建数据通道通信通道
        let (dc_tx, mut dc_rx) = mpsc::unbounded_channel();

        // 设置P2P连接超时（30秒）
        let client_for_timeout = Arc::clone(&client_arc);
        let _timeout_task = tokio::spawn(async move {
            tokio::time::sleep(tokio::time::Duration::from_secs(30)).await;

            let client = client_for_timeout.lock().await;
            // 检查是否还在连接中状态
            if matches!(client.connection_mode, ConnectionMode::P2PConnecting) {
                warn!("P2P连接超时，将继续使用服务器转发模式");
                client.notify_callback(|cb| {
                    cb.on_connection_state_changed("P2P连接超时，使用服务器转发".to_string());
                });
            }
        });

        // 启动消息接收循环
        let client_clone = Arc::clone(&client_arc);
        let message_task = tokio::spawn(async move {
            loop {
                tokio::select! {
                    // 处理WebSocket消息
                    msg = ws_receiver.next() => {
                        match msg {
                            Some(Ok(Message::Text(text))) => {
                                if let Ok(signal_msg) = serde_json::from_str::<SignalMessage>(&text) {
                                    // 处理信令消息，需要小心锁的作用域
                                    match signal_msg {
                                        SignalMessage::Joined { client_id: id } => {
                                            info!("已连接，客户端ID: {}", id);
                                            {
                                                let mut client = client_clone.lock().await;
                                                client.client_id = Some(id.clone());
                                                client.show_connection_mode();
                                                client.notify_callback(|cb| cb.on_connected(id));
                                            }
                                        }
                                        other_msg => {
                                            // 对于其他复杂消息，在新任务中处理
                                            let client_arc_clone = Arc::clone(&client_clone);
                                            let dc_tx_clone = dc_tx.clone();
                                            tokio::spawn(async move {
                                                let mut client = client_arc_clone.lock().await;
                                                handle_signal_message(&mut client, other_msg, dc_tx_clone).await;
                                            });
                                        }
                                    }
                                }
                            }
                            Some(Ok(Message::Binary(data))) => {
                                // 处理二进制WebSocket消息
                                info!("收到二进制WebSocket消息: {} 字节", data.len());
                                if data.len() >= 12 { // 至少需要 magic(4) + client_id_len(4) + room_id_len(4)
                                    // 检查魔数
                                    const MAGIC_NUMBER: u32 = 0xCAFEBABE;
                                    let magic = u32::from_le_bytes([data[0], data[1], data[2], data[3]]);
                                    
                                    if magic == MAGIC_NUMBER {
                                        info!("收到带魔数的直接二进制数据消息");
                                        // 处理带魔数的直接二进制数据消息
                                        // 解析格式：[magic(4字节)] + [client_id_length(4字节)] + [client_id] + [room_id_length(4字节)] + [room_id] + [data]
                                        let client_id_len = u32::from_le_bytes([data[4], data[5], data[6], data[7]]) as usize;
                                        if data.len() >= 12 + client_id_len {
                                            let room_id_len = u32::from_le_bytes([
                                                data[8 + client_id_len], 
                                                data[9 + client_id_len], 
                                                data[10 + client_id_len], 
                                                data[11 + client_id_len]
                                            ]) as usize;
                                            
                                            if data.len() >= 12 + client_id_len + room_id_len {
                                                let sender_id = String::from_utf8_lossy(&data[8..8 + client_id_len]).to_string();
                                                let binary_data = data[12 + client_id_len + room_id_len..].to_vec();
                                                
                                                info!("收到直接WebSocket二进制数据 [{}]: {} 字节", sender_id, binary_data.len());
                                                let client = client_clone.lock().await;
                                                // 只有当发送者不是自己时才触发回调
                                                if let Some(ref my_client_id) = client.client_id {
                                                    if sender_id != *my_client_id {
                                                        client.notify_callback(|cb| {
                                                            cb.on_binary_data_received(sender_id, binary_data, false);
                                                        });
                                                    }
                                                } else {
                                                    client.notify_callback(|cb| {
                                                        cb.on_binary_data_received(sender_id, binary_data, false);
                                                    });
                                                }
                                            }
                                        }
                                    } else if data.len() >= 8 {
                                        info!("收到旧格式二进制数据消息");
                                        // 处理旧格式的二进制消息（兼容性）
                                        // 解析格式：[client_id_length(4字节)] + [client_id] + [room_id_length(4字节)] + [room_id] + [data]
                                        let client_id_len = u32::from_le_bytes([data[0], data[1], data[2], data[3]]) as usize;
                                        if data.len() >= 8 + client_id_len {
                                            let room_id_len = u32::from_le_bytes([
                                                data[4 + client_id_len], 
                                                data[5 + client_id_len], 
                                                data[6 + client_id_len], 
                                                data[7 + client_id_len]
                                            ]) as usize;
                                            
                                            if data.len() >= 8 + client_id_len + room_id_len {
                                                let sender_id = String::from_utf8_lossy(&data[4..4 + client_id_len]).to_string();
                                                let binary_data = data[8 + client_id_len + room_id_len..].to_vec();
                                                
                                                info!("收到旧格式二进制数据 [{}]: {} 字节", sender_id, binary_data.len());
                                                let client = client_clone.lock().await;
                                                client.notify_callback(|cb| {
                                                    cb.on_binary_data_received(sender_id, binary_data, false);
                                                });
                                            }
                                        }
                                    }
                                }
                            }
                            Some(Ok(Message::Close(_))) | None => {
                                info!("WebSocket连接已关闭");
                                break;
                            }
                            Some(Err(e)) => {
                                error!("WebSocket错误: {}", e);
                                break;
                            }
                            _ => {}
                        }
                    }
                    
                    // 处理数据通道
                    dc = dc_rx.recv() => {
                        if let Some(data_channel) = dc {
                            let mut client = client_clone.lock().await;
                            client.set_received_data_channel(data_channel);
                        }
                    }
                    
                    // 处理关闭信号
                    _ = shutdown_rx.recv() => {
                        info!("收到关闭信号，停止消息处理");
                        break;
                    }
                }
            }
        });

        // 将任务句柄存储到客户端
        {
            let mut client = client_arc.lock().await;
            client.task_handles.push(message_task);
            client.task_handles.push(_timeout_task);
        }

        Ok(())
    }

    // 保留原来的connect方法作为内部使用
    pub async fn connect(&mut self) -> Result<(), Box<dyn std::error::Error>> {
        // 这个方法现在只是一个占位符，实际连接由connect_and_start处理
        Ok(())
    }

    // 断开连接
    pub async fn disconnect(&mut self) {
        self.info_log("断开连接");

        // 发送关闭信号
        if let Some(ref shutdown_tx) = self.shutdown_tx {
            let _ = shutdown_tx.send(());
        }

        // 关闭WebRTC连接
        if let Some(ref pc) = self.peer_connection {
            let _ = pc.close().await;
        }

        // 等待任务完成
        for handle in self.task_handles.drain(..) {
            let _ = handle.await;
        }

        // 重置状态
        self.reset_connection_state();
    }

    // 创建PeerConnection
    async fn create_peer_connection(&mut self) -> Result<(), Box<dyn std::error::Error>> {
        self.debug_log("创建PeerConnection");

        let api = Self::create_webrtc_api().await?;

        let mut ice_servers = vec![
            // 优先使用IPv4 STUN服务器，避免IPv6问题
            RTCIceServer {
                urls: vec![
                    "stun:stun.l.google.com:19302".to_owned(),
                    "stun:stun1.l.google.com:19302".to_owned()
                ],
                ..Default::default()
            },
            // 添加更多可靠的STUN服务器
            RTCIceServer {
                urls: vec![
                    "stun:stun.stunprotocol.org:3478".to_owned(),
                    "stun:stun.ekiga.net".to_owned()
                ],
                ..Default::default()
            },
            // 添加备用STUN服务器
            RTCIceServer {
                urls: vec![
                    "stun:stun.ideasip.com".to_owned(),
                    "stun:stun.rixtelecom.se".to_owned()
                ],
                ..Default::default()
            }
        ];

        // 添加自定义STUN服务器
        if !self.config.stun_servers.is_empty() {
            ice_servers.push(RTCIceServer {
                urls: self.config.stun_servers.clone(),
                ..Default::default()
            });
        }

        let config = RTCConfiguration {
            ice_servers,
            // 优化ICE配置以改善连接性
            ice_candidate_pool_size: 10,
            // 设置ICE传输策略，优先使用relay
            ice_transport_policy: webrtc::peer_connection::policy::ice_transport_policy::RTCIceTransportPolicy::All,
            // 设置bundle策略
            bundle_policy: webrtc::peer_connection::policy::bundle_policy::RTCBundlePolicy::Balanced,
            // 设置RTCP多路复用策略
            rtcp_mux_policy: webrtc::peer_connection::policy::rtcp_mux_policy::RTCRtcpMuxPolicy::Require,
            ..Default::default()
        };

        let peer_connection = Arc::new(api.new_peer_connection(config).await?);

        // 设置ICE候选回调
        let ws_sender_clone = self.ws_sender.clone();
        let room_id_clone = self.config.room_id.clone();
        let is_debug = self.config.debug;

        peer_connection.on_ice_candidate(
            Box::new(move |candidate| {
                let ws_sender = ws_sender_clone.clone();
                let room_id = room_id_clone.clone();

                Box::pin(async move {
                    if let Some(candidate) = candidate {
                        if is_debug {
                            debug!("发送ICE候选");
                        }

                        if let Some(ws_sender) = ws_sender {
                            let ice_msg = SignalMessage::IceCandidate {
                                room_id,
                                candidate: serde_json::to_string(&candidate).unwrap_or_default(),
                            };

                            if let Ok(json) = serde_json::to_string(&ice_msg) {
                                let mut sender = ws_sender.lock().await;
                                let _ = sender.send(Message::Text(json)).await;
                            }
                        }
                    }
                })
            })
        );

        // 设置连接状态变化回调
        let is_debug_clone = self.config.debug;
        peer_connection.on_peer_connection_state_change(
            Box::new(move |state| {
                Box::pin(async move {
                    match state {
                        RTCPeerConnectionState::Connected => {
                            info!("P2P连接已建立！");
                        }
                        RTCPeerConnectionState::Failed => {
                            warn!("P2P连接失败，切换到服务器转发模式");
                            if is_debug_clone {
                                debug!(
                                    "P2P连接失败原因可能：NAT穿透失败、防火墙阻塞、网络环境不支持"
                                );
                            }
                        }
                        RTCPeerConnectionState::Disconnected => {
                            warn!("P2P连接已断开，切换到服务器转发模式");
                        }
                        RTCPeerConnectionState::Closed => {
                            info!("P2P连接已关闭");
                        }
                        RTCPeerConnectionState::Connecting => {
                            if is_debug_clone {
                                debug!("正在建立P2P连接...");
                            }
                        }
                        _ => {
                            if is_debug_clone {
                                debug!("P2P连接状态: {:?}", state);
                            }
                        }
                    }
                })
            })
        );

        // 添加ICE连接状态监控
        let is_debug_ice = self.config.debug;
        let callback_for_ice = self.callback.clone();
        peer_connection.on_ice_connection_state_change(
            Box::new(move |state| {
                let callback_clone = callback_for_ice.clone();
                Box::pin(async move {
                    match state {
                        webrtc::ice_transport::ice_connection_state::RTCIceConnectionState::Connected => {
                            info!("ICE连接已建立 - P2P连接成功");
                            if let Some(ref cb) = callback_clone {
                                cb.on_connection_state_changed("P2P已连接".to_string());
                            }
                        }
                        webrtc::ice_transport::ice_connection_state::RTCIceConnectionState::Failed => {
                            warn!("ICE连接失败 - 将使用服务器转发模式");
                            if let Some(ref cb) = callback_clone {
                                cb.on_connection_state_changed(
                                    "P2P连接失败，使用服务器转发".to_string()
                                );
                            }
                        }
                        webrtc::ice_transport::ice_connection_state::RTCIceConnectionState::Disconnected => {
                            warn!("ICE连接已断开 - 切换到服务器转发模式");
                            if let Some(ref cb) = callback_clone {
                                cb.on_connection_state_changed("P2P连接断开".to_string());
                            }
                        }
                        webrtc::ice_transport::ice_connection_state::RTCIceConnectionState::Checking => {
                            info!("正在检查ICE连接...");
                            if let Some(ref cb) = callback_clone {
                                cb.on_connection_state_changed("正在建立P2P连接".to_string());
                            }
                        }
                        webrtc::ice_transport::ice_connection_state::RTCIceConnectionState::Completed => {
                            info!("ICE连接完成 - P2P连接优化完成");
                            if let Some(ref cb) = callback_clone {
                                cb.on_connection_state_changed("P2P连接已优化".to_string());
                            }
                        }
                        _ => {
                            if is_debug_ice {
                                debug!("ICE连接状态: {:?}", state);
                            }
                        }
                    }
                })
            })
        );

        self.peer_connection = Some(peer_connection);
        self.connection_mode = ConnectionMode::P2PConnecting;
        self.show_connection_mode();

        Ok(())
    }

    // 创建数据通道（作为发起方）
    async fn create_data_channel(&mut self) -> Result<(), Box<dyn std::error::Error>> {
        if let Some(ref pc) = self.peer_connection {
            self.debug_log("创建数据通道");

            let data_channel = pc.create_data_channel("chat", None).await?;

            // 设置数据通道回调
            let callback_clone = self.callback.clone();
            data_channel.on_open(
                Box::new(move || {
                    info!("数据通道已打开 - P2P连接建立");
                    if let Some(ref cb) = callback_clone {
                        cb.on_connection_state_changed("P2P已连接".to_string());
                    }
                    Box::pin(async {})
                })
            );

            let callback_clone2 = self.callback.clone();
            data_channel.on_message(
                Box::new(move |msg| {
                    if msg.is_string {
                        // 处理文本消息
                        if let Ok(text) = String::from_utf8(msg.data.to_vec()) {
                            info!("收到P2P文本消息: {}", text);
                            if let Some(ref cb) = callback_clone2 {
                                cb.on_message_received("peer".to_string(), text, true);
                            }
                        }
                    } else {
                        // 处理二进制数据
                        let data = msg.data.to_vec();
                        info!("收到P2P二进制数据: {} 字节", data.len());
                        if let Some(ref cb) = callback_clone2 {
                            cb.on_binary_data_received("peer".to_string(), data, true);
                        }
                    }
                    Box::pin(async {})
                })
            );

            self.data_channel = Some(data_channel);
            // 当数据通道创建成功时，我们处于连接中状态
            // 实际的P2PConnected状态会在数据通道打开时设置
        }

        Ok(())
    }

    // 手动设置接收到的数据通道（由外部调用）
    fn set_received_data_channel(&mut self, data_channel: Arc<RTCDataChannel>) {
        self.debug_log("设置接收到的数据通道");
        self.data_channel = Some(data_channel);
        self.connection_mode = ConnectionMode::P2PConnected;
        self.show_connection_mode();
    }

    // 检查数据通道状态并更新连接模式
    fn update_connection_status(&mut self) {
        if let Some(ref dc) = self.data_channel {
            if dc.ready_state() == RTCDataChannelState::Open {
                if !matches!(self.connection_mode, ConnectionMode::P2PConnected) {
                    self.connection_mode = ConnectionMode::P2PConnected;
                    self.show_connection_mode();
                }
            }
        }
    }

    // 创建Offer
    async fn create_offer(&mut self) -> Result<(), Box<dyn std::error::Error>> {
        if let Some(ref pc) = self.peer_connection {
            self.log_signaling_state("创建Offer前");
            self.debug_log("创建Offer");

            let offer = pc.create_offer(None).await?;
            pc.set_local_description(offer.clone()).await?;

            // 标记为发起方且已发送offer
            self.is_initiator = true;
            self.has_sent_offer = true;
            self.log_signaling_state("创建Offer后");

            if let Some(ref ws_sender) = self.ws_sender {
                let offer_msg = SignalMessage::Offer {
                    room_id: self.config.room_id.clone(),
                    sdp: offer.sdp,
                };

                if let Ok(json) = serde_json::to_string(&offer_msg) {
                    let mut sender = ws_sender.lock().await;
                    let _ = sender.send(Message::Text(json)).await;
                    self.debug_log("已发送Offer");
                }
            }
        }

        Ok(())
    }

    // 处理收到的Offer
    async fn handle_offer(&mut self, sdp: String) -> Result<(), Box<dyn std::error::Error>> {
        self.log_signaling_state("收到Offer");
        self.debug_log("处理收到的Offer");

        // 检查是否可以处理offer
        if !self.can_handle_offer() {
            self.debug_log("忽略offer - 已在处理另一个连接");
            return Ok(());
        }

        // 如果已经发送了offer，使用客户端ID比较决定谁作为发起方
        if self.has_sent_offer {
            if let Some(ref client_id) = self.client_id {
                // 使用客户端ID的哈希值比较，确定性地决定谁作为发起方
                use std::hash::{ Hash, Hasher };
                use std::collections::hash_map::DefaultHasher;

                let mut hasher = DefaultHasher::new();
                client_id.hash(&mut hasher);
                let my_hash = hasher.finish();

                let mut hasher2 = DefaultHasher::new();
                sdp.hash(&mut hasher2);
                let remote_hash = hasher2.finish();

                let should_be_initiator = my_hash < remote_hash;
                if should_be_initiator {
                    self.debug_log("保持发起方角色，忽略收到的offer");
                    return Ok(());
                } else {
                    self.debug_log("切换为应答方角色，重置连接状态");
                    // 重置状态，准备作为应答方
                    if let Some(ref pc) = self.peer_connection {
                        pc.close().await.ok();
                    }
                    self.peer_connection = None;
                    self.data_channel = None;
                    self.has_sent_offer = false;
                    self.is_initiator = false;
                }
            }
        }

        if self.peer_connection.is_none() {
            self.create_peer_connection().await?;
            // 注意：我们不在这里设置数据通道接收器，而是在主函数中处理
        }

        if let Some(ref pc) = self.peer_connection {
            let remote_desc = RTCSessionDescription::offer(sdp)?;
            pc.set_remote_description(remote_desc).await?;

            // 标记已收到offer
            self.has_received_offer = true;

            let answer = pc.create_answer(None).await?;
            pc.set_local_description(answer.clone()).await?;

            if let Some(ref ws_sender) = self.ws_sender {
                let answer_msg = SignalMessage::Answer {
                    room_id: self.config.room_id.clone(),
                    sdp: answer.sdp,
                };

                if let Ok(json) = serde_json::to_string(&answer_msg) {
                    let mut sender = ws_sender.lock().await;
                    let _ = sender.send(Message::Text(json)).await;
                    self.debug_log("已发送Answer");
                }
            }
        }

        Ok(())
    }

    // 处理收到的Answer
    async fn handle_answer(&mut self, sdp: String) -> Result<(), Box<dyn std::error::Error>> {
        self.log_signaling_state("收到Answer");
        self.debug_log("处理收到的Answer");

        // 检查是否可以处理answer
        if !self.can_handle_answer() {
            self.debug_log("忽略answer - 没有待处理的offer或不是发起方");
            return Ok(());
        }

        if let Some(ref pc) = self.peer_connection {
            let remote_desc = RTCSessionDescription::answer(sdp)?;
            pc.set_remote_description(remote_desc).await?;
            self.debug_log("成功设置远程Answer描述");
        }

        Ok(())
    }

    // 处理ICE候选
    async fn handle_ice_candidate(
        &mut self,
        candidate_str: String
    ) -> Result<(), Box<dyn std::error::Error>> {
        if let Some(ref pc) = self.peer_connection {
            if let Ok(candidate) = serde_json::from_str(&candidate_str) {
                self.debug_log("添加ICE候选");
                pc.add_ice_candidate(candidate).await?;
            }
        }

        Ok(())
    }

    // 发送文本消息（P2P优先，回退到服务器转发）
    pub async fn send_message(&mut self, message: &str) -> Result<(), Box<dyn std::error::Error>> {
        // 首先检查并更新连接状态
        self.update_connection_status();

        // 尝试P2P发送
        if let Some(ref dc) = self.data_channel {
            if dc.ready_state() == RTCDataChannelState::Open {
                self.info_log(&format!("通过P2P发送文本消息: {}", message));
                dc.send_text(message.to_string()).await?;
                return Ok(());
            }
        }

        // 回退到服务器转发
        self.send_text_via_websocket(message).await?;
        Ok(())
    }

    // 发送二进制数据（P2P优先，回退到服务器转发）
    pub async fn send_binary_data(
        &mut self,
        data: &[u8]
    ) -> Result<(), Box<dyn std::error::Error>> {
        // 首先检查并更新连接状态
        self.update_connection_status();

        // 尝试P2P发送
        if let Some(ref dc) = self.data_channel {
            if dc.ready_state() == RTCDataChannelState::Open {
                self.info_log(&format!("通过P2P发送二进制数据: {} 字节", data.len()));
                // 创建二进制消息 - 需要复制数据以满足生命周期要求
                let data_bytes = data.to_vec();
                let msg = DataChannelMessage {
                    is_string: false,
                    data: data_bytes.into(),
                };
                dc.send(&msg.data).await?;
                return Ok(());
            }
        }

        // 回退到服务器转发
        self.send_binary_via_websocket(data).await?;
        Ok(())
    }

    // 通过WebSocket发送文本消息
    async fn send_text_via_websocket(
        &self,
        message: &str
    ) -> Result<(), Box<dyn std::error::Error>> {
        if let (Some(ref ws_sender), Some(ref client_id)) = (&self.ws_sender, &self.client_id) {
            let chat_msg = SignalMessage::ChatMessage {
                room_id: self.config.room_id.clone(),
                message: message.to_string(),
                sender_id: client_id.clone(),
            };

            if let Ok(json) = serde_json::to_string(&chat_msg) {
                let mut sender = ws_sender.lock().await;
                sender.send(Message::Text(json)).await?;
                self.info_log(&format!("通过服务器转发文本消息: {}", message));
            }
        } else {
            return self.websocket_not_ready_error();
        }

        Ok(())
    }

    // 通过WebSocket发送二进制数据
    async fn send_binary_via_websocket(
        &self,
        data: &[u8]
    ) -> Result<(), Box<dyn std::error::Error>> {
        if let (Some(ref ws_sender), Some(ref client_id)) = (&self.ws_sender, &self.client_id) {
            // 创建二进制消息格式：[client_id_length(4字节)] + [client_id] + [room_id_length(4字节)] + [room_id] + [data]
            let client_id_bytes = client_id.as_bytes();
            let room_id_bytes = self.config.room_id.as_bytes();

            let mut binary_msg = Vec::new();
            binary_msg.extend_from_slice(&(client_id_bytes.len() as u32).to_le_bytes());
            binary_msg.extend_from_slice(client_id_bytes);
            binary_msg.extend_from_slice(&(room_id_bytes.len() as u32).to_le_bytes());
            binary_msg.extend_from_slice(room_id_bytes);
            binary_msg.extend_from_slice(data);

            let mut sender = ws_sender.lock().await;
            sender.send(Message::Binary(binary_msg)).await?;
            self.info_log(&format!("通过服务器转发二进制数据: {} 字节", data.len()));
        } else {
            return self.websocket_not_ready_error();
        }

        Ok(())
    }

    // 直接通过WebSocket发送二进制数据（带魔数标识）
    pub async fn send_binary_data_directly(
        &self,
        data: &[u8]
    ) -> Result<(), Box<dyn std::error::Error>> {
        if let (Some(ref ws_sender), Some(ref client_id)) = (&self.ws_sender, &self.client_id) {
            // 创建带魔数的消息格式：[magic(4字节)] + [client_id_length(4字节)] + [client_id] + [room_id_length(4字节)] + [room_id] + [data]
            const MAGIC_NUMBER: u32 = 0xcafebabe;
            let client_id_bytes = client_id.as_bytes();
            let room_id_bytes = self.config.room_id.as_bytes();

            let mut binary_msg = Vec::new();
            // 写入魔数
            binary_msg.extend_from_slice(&MAGIC_NUMBER.to_le_bytes());
            // 写入客户端ID长度和内容
            binary_msg.extend_from_slice(&(client_id_bytes.len() as u32).to_le_bytes());
            binary_msg.extend_from_slice(client_id_bytes);
            // 写入房间ID长度和内容
            binary_msg.extend_from_slice(&(room_id_bytes.len() as u32).to_le_bytes());
            binary_msg.extend_from_slice(room_id_bytes);
            // 写入实际数据
            binary_msg.extend_from_slice(data);

            let mut sender = ws_sender.lock().await;
            sender.send(Message::Binary(binary_msg)).await?;
            self.info_log(&format!("直接通过WebSocket发送二进制数据: {} 字节", data.len()));
        } else {
            return self.websocket_not_ready_error();
        }

        Ok(())
    }

    // WebSocket未就绪错误
    fn websocket_not_ready_error(&self) -> Result<(), Box<dyn std::error::Error>> {
        if self.ws_sender.is_none() {
            self.error_log("WebSocket发送器未初始化");
        }
        if self.client_id.is_none() {
            self.error_log("客户端ID未设置，无法发送数据");
        }
        Err("WebSocket发送器或客户端ID未就绪".into())
    }
}

// 默认WebRTC配置
impl Default for WebRTCConfig {
    fn default() -> Self {
        Self {
            room_id: "default".to_string(),
            server_url: "ws://127.0.0.1:9001".to_string(),
            debug: false,
            stun_servers: vec![
                "stun:stun.l.google.com:19302".to_string(),
                "stun:stun1.l.google.com:19302".to_string()
            ],
        }
    }
}

// 处理信令消息
async fn handle_signal_message(
    client: &mut AndroidWebRTCClient,
    message: SignalMessage,
    dc_tx: mpsc::UnboundedSender<Arc<RTCDataChannel>>
) {
    match message {
        SignalMessage::Joined { client_id: id } => {
            info!("已连接，客户端ID: {}", id);
            client.client_id = Some(id.clone());
            client.show_connection_mode();
            client.notify_callback(|cb| cb.on_connected(id));
        }

        SignalMessage::UserJoined { client_id: user_id } => {
            info!("用户 {} 加入了房间", user_id);
            client.notify_callback(|cb| cb.on_user_joined(user_id.clone()));

            // Android端始终作为发起方，简化连接逻辑
            if let Some(ref my_client_id) = client.client_id {
                info!("检测到用户加入 - 我的ID: {}, 对方ID: {}", my_client_id, user_id);
                // Android端始终发起P2P连接
                client.debug_log("Android端始终作为发起方创建P2P连接");
                //发起p2p连接
                // 作为发起方，创建P2P连接
                if let Err(e) = client.create_peer_connection().await {
                    client.error_log(&format!("创建P2P连接失败: {}", e));
                    client.notify_callback(|cb| cb.on_error(format!("创建P2P连接失败: {}", e)));
                    return;
                }

                if let Err(e) = client.create_data_channel().await {
                    client.error_log(&format!("创建数据通道失败: {}", e));
                    client.notify_callback(|cb| cb.on_error(format!("创建数据通道失败: {}", e)));
                    return;
                }

                if let Err(e) = client.create_offer().await {
                    client.error_log(&format!("创建Offer失败: {}", e));
                    client.notify_callback(|cb| cb.on_error(format!("创建Offer失败: {}", e)));
                }
            } else {
                // 兜底逻辑，如果没有客户端ID信息，按原逻辑处理
                client.debug_log("没有客户端ID信息，使用默认逻辑");

                if let Err(e) = client.create_peer_connection().await {
                    client.error_log(&format!("创建P2P连接失败: {}", e));
                    client.notify_callback(|cb| cb.on_error(format!("创建P2P连接失败: {}", e)));
                    return;
                }

                if let Err(e) = client.create_data_channel().await {
                    client.error_log(&format!("创建数据通道失败: {}", e));
                    client.notify_callback(|cb| cb.on_error(format!("创建数据通道失败: {}", e)));
                    return;
                }

                if let Err(e) = client.create_offer().await {
                    client.error_log(&format!("创建Offer失败: {}", e));
                    client.notify_callback(|cb| cb.on_error(format!("创建Offer失败: {}", e)));
                }
            }
        }

        SignalMessage::UserLeft { client_id: user_id } => {
            info!("用户 {} 离开了房间", user_id);
            client.notify_callback(|cb| cb.on_user_left(user_id));
            // 重置P2P连接状态
            client.reset_connection_state();
        }

        SignalMessage::Offer { sdp, .. } => {
            client.debug_log("收到Offer");

            // 在处理Offer前设置数据通道接收回调
            if client.peer_connection.is_none() {
                if let Err(e) = client.create_peer_connection().await {
                    client.error_log(&format!("创建P2P连接失败: {}", e));
                    client.notify_callback(|cb| cb.on_error(format!("创建P2P连接失败: {}", e)));
                    return;
                }

                // 设置数据通道接收回调
                if let Some(ref pc) = client.peer_connection {
                    let dc_tx_clone = dc_tx.clone();
                    let callback_clone = client.callback.clone();
                    pc.on_data_channel(
                        Box::new(move |dc| {
                            let dc_tx = dc_tx_clone.clone();
                            let callback_clone2 = callback_clone.clone();

                            Box::pin(async move {
                                info!("收到对方数据通道");

                                // 设置数据通道回调
                                let callback_clone3 = callback_clone2.clone();
                                dc.on_open(
                                    Box::new(move || {
                                        info!("数据通道已打开 - P2P连接建立");
                                        if let Some(ref cb) = callback_clone3 {
                                            cb.on_connection_state_changed("P2P已连接".to_string());
                                        }
                                        Box::pin(async {})
                                    })
                                );

                                let callback_clone4 = callback_clone2.clone();
                                dc.on_message(
                                    Box::new(move |msg| {
                                        if msg.is_string {
                                            // 处理文本消息
                                            if let Ok(text) = String::from_utf8(msg.data.to_vec()) {
                                                info!("收到P2P文本消息: {}", text);
                                                if let Some(ref cb) = callback_clone4 {
                                                    cb.on_message_received(
                                                        "peer".to_string(),
                                                        text,
                                                        true
                                                    );
                                                }
                                            }
                                        } else {
                                            // 处理二进制数据
                                            let data = msg.data.to_vec();
                                            info!("收到P2P二进制数据: {} 字节", data.len());
                                            if let Some(ref cb) = callback_clone4 {
                                                cb.on_binary_data_received(
                                                    "peer".to_string(),
                                                    data,
                                                    true
                                                );
                                            }
                                        }
                                        Box::pin(async {})
                                    })
                                );

                                // 通过通道发送数据通道引用给主循环
                                let _ = dc_tx.send(dc);
                            })
                        })
                    );
                }
            }

            if let Err(e) = client.handle_offer(sdp).await {
                client.error_log(&format!("处理Offer失败: {}", e));
                client.notify_callback(|cb| cb.on_error(format!("处理Offer失败: {}", e)));
            }
        }

        SignalMessage::Answer { sdp, .. } => {
            client.debug_log("收到Answer");
            if let Err(e) = client.handle_answer(sdp).await {
                client.error_log(&format!("处理Answer失败: {}", e));
                client.notify_callback(|cb| cb.on_error(format!("处理Answer失败: {}", e)));
            }
        }

        SignalMessage::IceCandidate { candidate, .. } => {
            client.debug_log("收到ICE候选");
            if let Err(e) = client.handle_ice_candidate(candidate).await {
                client.error_log(&format!("处理ICE候选失败: {}", e));
                client.notify_callback(|cb| cb.on_error(format!("处理ICE候选失败: {}", e)));
            }
        }

        SignalMessage::ChatMessage { message: msg, sender_id, .. } => {
            info!("收到转发消息 [{}]: {}", sender_id, msg);
            client.notify_callback(|cb| cb.on_message_received(sender_id, msg, false));
        }

        SignalMessage::Error { message: err_msg } => {
            error!("服务器错误: {}", err_msg);
            client.notify_callback(|cb| cb.on_error(err_msg));
        }

        SignalMessage::Join { .. } => {
            // 已经处理过的加入消息，可以忽略
        }
    }
}
