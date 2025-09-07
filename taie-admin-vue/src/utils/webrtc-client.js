/**
 * WebRTC P2P 通信客户端模块
 * 支持与Android端的WebRTC连接，Web端等待Android端发起连接
 * 
 * @author AI Assistant
 * @version 1.0.0
 */

class WebRTCClient {
    /**
     * 构造函数
     * @param {Object} options - 配置选项
     * @param {string} options.serverUrl - WebSocket信令服务器地址，默认 'ws://127.0.0.1:9001'
     * @param {Array} options.iceServers - STUN服务器配置
     * @param {boolean} options.debug - 是否开启调试模式
     * @param {boolean} options.autoInitiateP2P - 是否自动发起P2P连接，默认false（等待对方发起）
     * @param {Object} options.callbacks - 事件回调函数
     */
    constructor(options = {}) {
        // 基础属性
        this.ws = null;
        this.pc = null;
        this.dataChannel = null;
        this.roomId = null;
        this.clientId = null;
        this.debugLog = [];
        
        // 配置选项
        this.serverUrl = options.serverUrl || 'ws://127.0.0.1:9001';
        this.debug = options.debug || false;
        this.autoInitiateP2P = options.autoInitiateP2P || false; // 是否自动发起P2P连接
        
        // 信令状态追踪
        this.isInitiator = false;        // 是否为连接发起方
        this.hasSentOffer = false;       // 是否已发送offer
        this.hasReceivedOffer = false;   // 是否已收到offer
        
        // WebRTC配置
        this.pcConfig = {
            iceServers: options.iceServers || [
                { urls: 'stun:stun.l.google.com:19302' },
                { urls: 'stun:stun1.l.google.com:19302' }
            ]
        };
        
        // 事件回调
        this.callbacks = {
            onConnected: options.callbacks?.onConnected || (() => {}),
            onDisconnected: options.callbacks?.onDisconnected || (() => {}),
            onUserJoined: options.callbacks?.onUserJoined || (() => {}),
            onUserLeft: options.callbacks?.onUserLeft || (() => {}),
            onMessageReceived: options.callbacks?.onMessageReceived || (() => {}),
            onBinaryDataReceived: options.callbacks?.onBinaryDataReceived || (() => {}),
            onConnectionStateChanged: options.callbacks?.onConnectionStateChanged || (() => {}),
            onError: options.callbacks?.onError || (() => {}),
            onDebug: options.callbacks?.onDebug || (() => {})
        };
    }
    
    /**
     * 调试日志
     * @param {string} message - 日志消息
     */
    debug(message) {
        const timestamp = new Date().toLocaleTimeString();
        const logMessage = `[${timestamp}] ${message}`;
        this.debugLog.push(logMessage);
        
        if (this.debug) {
            console.log(`WebRTC DEBUG: ${message}`);
        }
        
        this.callbacks.onDebug(logMessage);
    }
    
    /**
     * 记录信令状态
     * @param {string} action - 动作描述
     */
    logSignalingState(action) {
        const state = `${action} - 发起方:${this.isInitiator} 已发送offer:${this.hasSentOffer} 已收到offer:${this.hasReceivedOffer}`;
        this.debug(state);
    }
    
    /**
     * 检查是否可以处理offer
     * @returns {boolean}
     */
    canHandleOffer() {
        return !this.hasReceivedOffer;
    }
    
    /**
     * 检查是否可以处理answer
     * @returns {boolean}
     */
    canHandleAnswer() {
        return this.hasSentOffer && this.pc;
    }
    
    /**
     * 重置连接状态
     */
    resetConnectionState() {
        if (this.pc) {
            this.pc.close();
        }
        this.pc = null;
        this.dataChannel = null;
        this.isInitiator = false;
        this.hasSentOffer = false;
        this.hasReceivedOffer = false;
        this.callbacks.onConnectionStateChanged('服务器转发模式');
    }
    
    /**
     * 连接到指定房间
     * @param {string} roomId - 房间ID
     * @returns {Promise<boolean>} 连接是否成功
     */
    async connect(roomId) {
        return new Promise((resolve, reject) => {
            this.roomId = roomId;
            this.debug('开始连接...');
            
            try {
                this.ws = new WebSocket(this.serverUrl);
                
                this.ws.onopen = () => {
                    this.debug('WebSocket连接已建立');
                    this.callbacks.onConnected();
                    
                    // 立即发送join消息
                    this.sendSignalMessage({
                        type: 'join',
                        room_id: this.roomId
                    });
                    
                    resolve(true);
                };
                
                this.ws.onmessage = async (event) => {
                    try {
                        // 检查是否为二进制数据
                        if (event.data instanceof ArrayBuffer) {
                            await this.handleBinaryMessage(event.data);
                        } else if (event.data instanceof Blob) {
                            await this.handleBinaryMessage(await event.data.arrayBuffer());
                        }
                        else {
                            const message = JSON.parse(event.data);
                            this.debug(`收到消息: ${message.type}`);
                            await this.handleMessage(message);
                        }
                    } catch (error) {
                        this.debug(`处理消息错误: ${error.message}`);
                        this.callbacks.onError(`处理消息错误: ${error.message}`);
                    }
                };
                
                this.ws.onclose = () => {
                    this.debug('WebSocket连接已关闭');
                    this.callbacks.onDisconnected();
                };
                
                this.ws.onerror = (error) => {
                    this.debug(`WebSocket错误: ${error}`);
                    this.callbacks.onError(`WebSocket错误: ${error}`);
                    reject(error);
                };
                
            } catch (error) {
                this.callbacks.onError(`连接失败: ${error.message}`);
                reject(error);
            }
        });
    }
    
    /**
     * 处理接收到的二进制消息（带魔数检查）
     * @param {ArrayBuffer} data - 二进制数据
     */
    async handleBinaryMessage(data) {
        try {
            if (data.byteLength < 12) { // 至少需要 magic(4) + client_id_len(4) + room_id_len(4)
                this.debug('二进制消息格式错误：数据太短');
                return;
            }
            
            const view = new DataView(data);
            const uint8View = new Uint8Array(data);
            let offset = 0;
            
            // 读取魔数
            const magicNumber = view.getUint32(offset, true);
            offset += 4;
            
            const MAGIC_NUMBER = 0xCAFEBABE;
            if (magicNumber === MAGIC_NUMBER) {
                // 处理带魔数的直接二进制数据消息
                // 读取客户端ID长度和内容
                const clientIdLength = view.getUint32(offset, true);
                offset += 4;
                
                if (data.byteLength < offset + clientIdLength + 4) {
                    this.debug('二进制消息格式错误：客户端ID数据不完整');
                    return;
                }
                
                const clientIdBytes = uint8View.slice(offset, offset + clientIdLength);
                const senderId = new TextDecoder().decode(clientIdBytes);
                offset += clientIdLength;
                
                // 读取房间ID长度和内容
                const roomIdLength = view.getUint32(offset, true);
                offset += 4;
                
                if (data.byteLength < offset + roomIdLength) {
                    this.debug('二进制消息格式错误：房间ID数据不完整');
                    return;
                }
                
                // 跳过房间ID
                offset += roomIdLength;
                
                // 提取实际的二进制数据
                const binaryData = uint8View.slice(offset);
                
                this.debug(`收到直接WebSocket二进制数据 [${senderId}]: ${binaryData.length} 字节`);
                
                // 只有当发送者不是自己时才触发回调
                if (senderId !== this.clientId) {
                    this.callbacks.onBinaryDataReceived(senderId, binaryData, false);
                }
            } else {
                // 尝试处理旧格式的二进制消息（兼容性）
                // 重置offset到开始位置，因为可能是旧格式
                offset = 0;
                if (data.byteLength >= 8) {
                    // 解析旧格式：[client_id_length(4字节)] + [client_id] + [room_id_length(4字节)] + [room_id] + [data]
                    const clientIdLen = view.getUint32(offset, true);
                    offset += 4;
                    
                    if (data.byteLength >= 8 + clientIdLen) {
                        const roomIdLen = view.getUint32(offset + clientIdLen, true);
                        
                        if (data.byteLength >= 8 + clientIdLen + roomIdLen) {
                            const senderId = new TextDecoder().decode(uint8View.slice(offset, offset + clientIdLen));
                            const binaryData = uint8View.slice(8 + clientIdLen + roomIdLen);
                            
                            this.debug(`收到旧格式二进制数据 [${senderId}]: ${binaryData.length} 字节`);
                            this.callbacks.onBinaryDataReceived(senderId, binaryData, false);
                        }
                    }
                }
            }
            
        } catch (error) {
            this.debug(`处理二进制消息错误: ${error.message}`);
            this.callbacks.onError(`处理二进制消息错误: ${error.message}`);
        }
    }
    
    /**
     * 处理接收到的消息
     * @param {Object} message - 信令消息
     */
    async handleMessage(message) {
        switch (message.type) {
            case 'joined':
                this.clientId = message.client_id;
                this.debug(`获得客户端ID: ${this.clientId}`);
                this.callbacks.onConnectionStateChanged('服务器转发模式');
                break;
                
            case 'user-joined':
                this.debug(`用户加入: ${message.client_id}`);
                this.callbacks.onUserJoined(message.client_id);
                
                // 根据配置决定是否主动发起P2P连接
                if (this.clientId && message.client_id) {
                    this.debug(`检测到用户加入: ${message.client_id}`);
                    
                    if (this.autoInitiateP2P) {
                        this.debug('自动发起P2P连接模式');
                        // 使用客户端ID比较决定谁作为发起方，避免双方同时发起
                        if (this.clientId < message.client_id) {
                            this.debug('作为发起方，主动创建P2P连接');
                            await this.initiateP2PConnection();
                        } else {
                            this.debug('作为接收方，等待对方发起P2P连接');
                        }
                    } else {
                        this.debug('Web端等待Android端发起P2P连接');
                        // 不创建offer，等待Android端的offer
                    }
                }
                break;
                
            case 'user-left':
                this.debug(`用户离开: ${message.client_id}`);
                this.callbacks.onUserLeft(message.client_id);
                this.resetConnectionState();
                break;
                
            case 'offer':
                this.debug('收到Offer');
                await this.handleOffer(message.sdp);
                break;
                
            case 'answer':
                this.debug('收到Answer');
                await this.handleAnswer(message.sdp);
                break;
                
            case 'ice-candidate':
                this.debug('收到ICE候选');
                await this.handleIceCandidate(message.candidate);
                break;
                
            case 'chat-message':
                if (message.sender_id !== this.clientId) {
                    this.callbacks.onMessageReceived(message.sender_id, message.message, false);
                    this.debug('收到服务器转发消息');
                }
                break;
                
            case 'error':
                this.debug(`服务器错误: ${message.message}`);
                this.callbacks.onError(message.message);
                break;
        }
    }
    
    /**
     * 主动发起P2P连接（内部方法）
     */
    async initiateP2PConnection() {
        try {
            this.debug('开始发起P2P连接');
            
            // 创建PeerConnection
            await this.createPeerConnection();
            
            // 创建DataChannel
            this.dataChannel = this.pc.createDataChannel('chat', { ordered: true });
            
            this.dataChannel.onopen = () => {
                this.debug('数据通道已打开');
                this.callbacks.onConnectionStateChanged('P2P直连模式');
            };
            
            this.dataChannel.onmessage = (event) => {
                this.debug('收到P2P消息');
                if (typeof event.data === 'string') {
                    this.callbacks.onMessageReceived('peer', event.data, true);
                } else {
                    // 处理二进制数据
                    this.callbacks.onBinaryDataReceived('peer', event.data, true);
                }
            };
            
            // 创建并发送Offer
            await this.createOffer();
            
        } catch (error) {
            this.debug(`发起P2P连接失败: ${error.message}`);
            this.callbacks.onError(`发起P2P连接失败: ${error.message}`);
        }
    }
    
    /**
     * 手动发起P2P连接（公共方法）
     * @param {string} targetClientId - 目标客户端ID（可选）
     * @returns {boolean} 是否成功发起连接
     */
    async manualInitiateP2P(targetClientId = null) {
        if (!this.clientId) {
            this.debug('客户端未连接，无法发起P2P连接');
            this.callbacks.onError('客户端未连接，无法发起P2P连接');
            return false;
        }
        
        if (this.pc && this.pc.connectionState === 'connected') {
            this.debug('P2P连接已存在');
            return true;
        }
        
        try {
            this.debug(`手动发起P2P连接${targetClientId ? ` (目标: ${targetClientId})` : ''}`);
            await this.initiateP2PConnection();
            return true;
        } catch (error) {
            this.debug(`手动发起P2P连接失败: ${error.message}`);
            this.callbacks.onError(`手动发起P2P连接失败: ${error.message}`);
            return false;
        }
    }
    
    /**
     * 创建Offer
     */
    async createOffer() {
        try {
            this.logSignalingState('创建Offer前');
            this.debug('创建Offer');
            
            const offer = await this.pc.createOffer();
            await this.pc.setLocalDescription(offer);
            
            // 标记为发起方且已发送offer
            this.isInitiator = true;
            this.hasSentOffer = true;
            this.logSignalingState('创建Offer后');
            
            this.sendSignalMessage({
                type: 'offer',
                room_id: this.roomId,
                sdp: offer.sdp
            });
        } catch (error) {
            this.debug(`创建Offer失败: ${error.message}`);
            this.callbacks.onError(`创建Offer失败: ${error.message}`);
        }
    }
    
    /**
     * 创建PeerConnection
     */
    async createPeerConnection() {
        this.debug('创建PeerConnection');
        this.pc = new RTCPeerConnection(this.pcConfig);
        
        this.pc.onicecandidate = (event) => {
            if (event.candidate) {
                this.debug('发送ICE候选');
                this.sendSignalMessage({
                    type: 'ice-candidate',
                    room_id: this.roomId,
                    candidate: JSON.stringify(event.candidate)
                });
            }
        };
        
        this.pc.onconnectionstatechange = () => {
            this.debug(`连接状态变化: ${this.pc.connectionState}`);
            if (this.pc.connectionState === 'connected') {
                this.callbacks.onConnectionStateChanged('P2P直连模式');
            }
        };
        
        this.pc.onicegatheringstatechange = () => {
            this.debug(`ICE收集状态: ${this.pc.iceGatheringState}`);
        };
        
        this.pc.oniceconnectionstatechange = () => {
            this.debug(`ICE连接状态: ${this.pc.iceConnectionState}`);
        };
        
        this.pc.ondatachannel = (event) => {
            this.debug('收到对方数据通道');
            const channel = event.channel;
            if (!this.dataChannel) {
                this.dataChannel = channel;
            }
            channel.onopen = () => {
                this.debug('对方数据通道已打开');
                this.callbacks.onConnectionStateChanged('P2P直连模式');
            };
            channel.onmessage = (event) => {
                this.debug('收到P2P消息');
                if (typeof event.data === 'string') {
                    this.callbacks.onMessageReceived('peer', event.data, true);
                } else {
                    // 处理二进制数据
                    this.callbacks.onBinaryDataReceived('peer', event.data, true);
                }
            };
        };
    }
    
    /**
     * 处理收到的Offer
     * @param {string} sdp - SDP描述
     */
    async handleOffer(sdp) {
        try {
            this.logSignalingState('收到Offer');
            
            // 检查是否可以处理offer
            if (!this.canHandleOffer()) {
                this.debug('忽略offer - 已在处理另一个连接');
                return;
            }
            
            // 如果已经发送了offer，使用客户端ID比较决定谁作为发起方
            if (this.hasSentOffer && this.clientId) {
                // 简单的字符串比较来确定性地决定角色
                const shouldBeInitiator = this.clientId < sdp.substring(0, this.clientId.length);
                if (shouldBeInitiator) {
                    this.debug('保持发起方角色，忽略收到的offer');
                    return;
                } else {
                    this.debug('切换为应答方角色，重置连接状态');
                    this.resetConnectionState();
                    await this.createPeerConnection();
                }
            }
            
            if (!this.pc) {
                await this.createPeerConnection();
            }
            
            this.debug('处理Offer');
            await this.pc.setRemoteDescription({ type: 'offer', sdp });
            
            // 标记已收到offer
            this.hasReceivedOffer = true;
            
            const answer = await this.pc.createAnswer();
            await this.pc.setLocalDescription(answer);
            this.sendSignalMessage({
                type: 'answer',
                room_id: this.roomId,
                sdp: answer.sdp
            });
        } catch (error) {
            this.debug(`处理Offer失败: ${error.message}`);
            this.callbacks.onError(`处理Offer失败: ${error.message}`);
        }
    }
    
    /**
     * 处理收到的Answer
     * @param {string} sdp - SDP描述
     */
    async handleAnswer(sdp) {
        try {
            this.logSignalingState('收到Answer');
            
            // 检查是否可以处理answer
            if (!this.canHandleAnswer()) {
                this.debug('忽略answer - 没有待处理的offer或不是发起方');
                return;
            }
            
            this.debug('处理Answer');
            await this.pc.setRemoteDescription({ type: 'answer', sdp });
            this.debug('成功设置远程Answer描述');
        } catch (error) {
            this.debug(`处理Answer失败: ${error.message}`);
            this.callbacks.onError(`处理Answer失败: ${error.message}`);
        }
    }
    
    /**
     * 处理ICE候选
     * @param {string} candidateStr - ICE候选JSON字符串
     */
    async handleIceCandidate(candidateStr) {
        try {
            const candidate = JSON.parse(candidateStr);
            this.debug('添加ICE候选');
            await this.pc.addIceCandidate(candidate);
        } catch (error) {
            this.debug(`处理ICE候选失败: ${error.message}`);
            this.callbacks.onError(`处理ICE候选失败: ${error.message}`);
        }
    }
    
    /**
     * 发送信令消息
     * @param {Object} message - 信令消息
     */
    sendSignalMessage(message) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(message));
        }
    }
    
    /**
     * 发送文本消息
     * @param {string} message - 要发送的消息
     * @returns {boolean} 发送是否成功
     */
    sendMessage(message) {
        if (!message || !message.trim()) {
            return false;
        }
        
        if (this.dataChannel && this.dataChannel.readyState === 'open') {
            this.debug('通过P2P发送消息');
            this.dataChannel.send(message);
            return true;
        } else if (this.ws && this.ws.readyState === WebSocket.OPEN && this.roomId) {
            this.debug('通过服务器转发消息');
            this.sendSignalMessage({
                type: 'chat-message',
                room_id: this.roomId,
                message: message,
                sender_id: this.clientId
            });
            return true;
        }
        
        return false;
    }
    
    /**
     * 发送二进制数据
     * @param {ArrayBuffer|Uint8Array|Blob} data - 要发送的二进制数据
     * @returns {boolean} 发送是否成功
     */
    sendBinaryData(data) {
        if (!data) {
            return false;
        }
        
        if (this.dataChannel && this.dataChannel.readyState === 'open') {
            this.debug(`通过P2P发送二进制数据: ${data.byteLength || data.size} 字节`);
            this.dataChannel.send(data);
            return true;
        } else {
            this.debug('P2P连接未就绪，无法发送二进制数据');
            this.callbacks.onError('P2P连接未就绪，无法发送二进制数据');
            return false;
        }
    }
    
    /**
     * 直接通过WebSocket发送二进制数据（带魔数标识）
     * @param {ArrayBuffer|Uint8Array|Blob} data - 要发送的二进制数据
     * @returns {Promise<boolean>} 发送是否成功
     */
    async sendBinaryDataDirectly(data) {
        if (!data) {
            return false;
        }
        
        if (!this.ws || this.ws.readyState !== WebSocket.OPEN || !this.roomId || !this.clientId) {
            this.debug('WebSocket连接未就绪，无法发送二进制数据');
            this.callbacks.onError('WebSocket连接未就绪，无法发送二进制数据');
            return false;
        }
        
        try {
            // 将数据转换为ArrayBuffer
            let arrayBuffer;
            if (data instanceof ArrayBuffer) {
                arrayBuffer = data;
            } else if (data instanceof Uint8Array) {
                arrayBuffer = data.buffer.slice(data.byteOffset, data.byteOffset + data.byteLength);
            } else if (data instanceof Blob) {
                arrayBuffer = await data.arrayBuffer();
            } else {
                this.debug('不支持的二进制数据类型');
                this.callbacks.onError('不支持的二进制数据类型');
                return false;
            }
            
            // 创建带魔数的消息格式：[magic(4字节)] + [client_id_length(4字节)] + [client_id] + [room_id_length(4字节)] + [room_id] + [data]
            const MAGIC_NUMBER = 0xCAFEBABE;
            const clientIdBytes = new TextEncoder().encode(this.clientId);
            const roomIdBytes = new TextEncoder().encode(this.roomId);
            
            const totalLength = 4 + 4 + clientIdBytes.length + 4 + roomIdBytes.length + arrayBuffer.byteLength;
            const binaryMessage = new ArrayBuffer(totalLength);
            const view = new DataView(binaryMessage);
            const uint8View = new Uint8Array(binaryMessage);
            
            let offset = 0;
            
            // 写入魔数
            view.setUint32(offset, MAGIC_NUMBER, true); // little endian
            offset += 4;
            
            // 写入客户端ID长度和内容
            view.setUint32(offset, clientIdBytes.length, true);
            offset += 4;
            uint8View.set(clientIdBytes, offset);
            offset += clientIdBytes.length;
            
            // 写入房间ID长度和内容
            view.setUint32(offset, roomIdBytes.length, true);
            offset += 4;
            uint8View.set(roomIdBytes, offset);
            offset += roomIdBytes.length;
            
            // 写入二进制数据
            uint8View.set(new Uint8Array(arrayBuffer), offset);
            
            this.ws.send(binaryMessage);
            this.debug(`直接通过WebSocket发送二进制数据: ${arrayBuffer.byteLength} 字节`);
            return true;
            
        } catch (error) {
            this.debug(`发送二进制数据失败: ${error.message}`);
            this.callbacks.onError(`发送二进制数据失败: ${error.message}`);
            return false;
        }
    }
    
    /**
     * 运行调试检查
     * @returns {Object} 调试信息
     */
    runDebugCheck() {
        const debugInfo = {
            websocketState: this.ws ? this.ws.readyState : null,
            peerConnectionState: this.pc ? this.pc.connectionState : null,
            iceConnectionState: this.pc ? this.pc.iceConnectionState : null,
            dataChannelState: this.dataChannel ? this.dataChannel.readyState : null,
            roomId: this.roomId,
            clientId: this.clientId,
            isInitiator: this.isInitiator,
            hasSentOffer: this.hasSentOffer,
            hasReceivedOffer: this.hasReceivedOffer
        };
        
        this.debug('=== 调试检查开始 ===');
        Object.entries(debugInfo).forEach(([key, value]) => {
            this.debug(`${key}: ${value}`);
        });
        this.logSignalingState('调试检查');
        this.debug('=== 调试检查结束 ===');
        
        return debugInfo;
    }
    
    /**
     * 断开连接
     */
    disconnect() {
        this.debug('断开连接');
        
        if (this.ws) {
            this.ws.close();
        }
        
        this.resetConnectionState();
        this.roomId = null;
        this.clientId = null;
        this.callbacks.onDisconnected();
    }
    
    /**
     * 强制重新建立P2P连接
     * @returns {boolean} 是否成功发起重连
     */
    async forceReconnectP2P() {
        this.debug('强制重新建立P2P连接');
        this.resetConnectionState();
        return await this.manualInitiateP2P();
    }
    
    /**
     * 设置自动发起P2P连接模式
     * @param {boolean} enabled - 是否启用自动发起模式
     */
    setAutoInitiateP2P(enabled) {
        this.autoInitiateP2P = enabled;
        this.debug(`自动发起P2P连接模式: ${enabled ? '启用' : '禁用'}`);
    }
    
    /**
     * 检查是否可以发起P2P连接
     * @returns {boolean} 是否可以发起连接
     */
    canInitiateP2P() {
        return this.clientId && 
               this.ws && 
               this.ws.readyState === WebSocket.OPEN && 
               (!this.pc || this.pc.connectionState !== 'connected');
    }
    
    /**
     * 获取P2P连接状态详情
     * @returns {Object} P2P连接状态详情
     */
    getP2PConnectionDetails() {
        return {
            hasDataChannel: !!this.dataChannel,
            dataChannelState: this.dataChannel ? this.dataChannel.readyState : null,
            peerConnectionState: this.pc ? this.pc.connectionState : null,
            iceConnectionState: this.pc ? this.pc.iceConnectionState : null,
            iceGatheringState: this.pc ? this.pc.iceGatheringState : null,
            signalingState: this.pc ? this.pc.signalingState : null,
            isInitiator: this.isInitiator,
            hasSentOffer: this.hasSentOffer,
            hasReceivedOffer: this.hasReceivedOffer
        };
    }
    
    /**
     * 获取连接状态
     * @returns {Object} 连接状态信息
     */
    getConnectionState() {
        return {
            connected: this.ws && this.ws.readyState === WebSocket.OPEN,
            p2pConnected: this.dataChannel && this.dataChannel.readyState === 'open',
            roomId: this.roomId,
            clientId: this.clientId,
            autoInitiateP2P: this.autoInitiateP2P,
            canInitiateP2P: this.canInitiateP2P()
        };
    }
    
    /**
     * 获取调试日志
     * @param {number} limit - 返回的日志条数限制
     * @returns {Array<string>} 调试日志数组
     */
    getDebugLog(limit = 50) {
        return this.debugLog.slice(-limit);
    }
    
    /**
     * 清空调试日志
     */
    clearDebugLog() {
        this.debugLog = [];
    }
}

// 如果在Node.js环境中，导出模块
if (typeof module !== 'undefined' && module.exports) {
    module.exports = WebRTCClient;
}
export default WebRTCClient;
// 如果在浏览器环境中，添加到全局对象
if (typeof window !== 'undefined') {
    window.WebRTCClient = WebRTCClient;
}
