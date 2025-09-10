package com.example.aslib;

import android.util.Log;

import com.example.aslib.common.AppConfig;
import com.example.aslib.common.Constant;
import com.example.aslib.common.Utils;
import com.example.aslib.vo.HandlerMessage;
import com.google.protobuf.MessageLite;

import java.nio.ByteBuffer;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.renren.modules.app.message.proto.Message;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.WebSocket;

public class NetWork {
    private final AtomicReference<WebSocket> wsRef = new AtomicReference<>();
    private final String TAG = "ASSIST-NETWORK";
    private final String deviceId;
    private String roomId;
    private long lastHeartbeat;
    private ScheduledExecutorService heartbeatScheduler;
    private ScheduledExecutorService reconnectScheduler;

    // 重连相关状态
    private volatile boolean isReconnecting = false;
    private volatile boolean shouldReconnect = true;
    private volatile int reconnectAttempts = 0;
    private volatile long lastReconnectTime = 0;

    // 重连配置参数
    private static final int MAX_RECONNECT_ATTEMPTS = 5;           // 最大重连次数
    private static final int INITIAL_RECONNECT_DELAY = 1000;       // 初始重连延迟（毫秒）
    private static final int MAX_RECONNECT_DELAY = 30000;          // 最大重连延迟（毫秒）
    private static final double RECONNECT_BACKOFF_MULTIPLIER = 1.5; // 重连延迟倍数
    private static final int CONNECTION_TIMEOUT = 60000;           // 连接超时时间（毫秒）

    // 消息类型常量 - 匹配服务端协议
    private static final int MSG_TYPE_ROOM_NOTIFICATION = 0x00000001;  // 房间通知消息
    private static final int MSG_TYPE_CLIENT_MESSAGE = 0x00000002;     // 客户端消息
    private static final int MSG_TYPE_HEARTBEAT = 0x00000003;          // 心跳消息

    // 房间通知子类型
    private static final byte ROOM_EVENT_CLIENT_JOINED = 0x01;
    private static final byte ROOM_EVENT_CLIENT_LEFT = 0x02;
    private static final byte ROOM_EVENT_CLIENT_ERROR = 0x03;
    private static final byte ROOM_EVENT_ROOM_MEMBER_COUNT = 0x04;

    // 心跳间隔（秒）
    private static final int HEARTBEAT_INTERVAL = 30;

    // 回调接口
    public interface NetworkListener {
        void onClientJoined(String sessionId);

        void onClientLeft(String sessionId);

        void onClientError(String sessionId);

        void onRoomMemberCount(int count);

        void onClientMessage(byte[] message);

        void onConnectionClosed();

        void onConnectionError(Throwable error);

        // 重连相关回调
        void onReconnectStarted(int attempt);           // 开始重连

        void onReconnectSuccess(int attempts);          // 重连成功

        void onReconnectFailed(int attempt, Throwable error); // 重连失败

        void onReconnectGiveUp(int totalAttempts);      // 放弃重连
    }

    private NetworkListener listener;

    public NetWork(String deviceId, String roomId, NetworkListener listener) {
        this.deviceId = deviceId;
        this.roomId = roomId;
        this.lastHeartbeat = System.currentTimeMillis();
        this.listener = listener;
        initSchedulers();
    }

    /**
     * 初始化调度器
     */
    private void initSchedulers() {
        // 初始化心跳调度器
        heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
        heartbeatScheduler.scheduleWithFixedDelay(this::sendHeartbeat, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);

        // 初始化重连调度器
        reconnectScheduler = Executors.newSingleThreadScheduledExecutor();

        // 启动连接监控任务
        startConnectionMonitoring();
    }

    /**
     * 启动连接监控
     */
    private void startConnectionMonitoring() {
        heartbeatScheduler.scheduleWithFixedDelay(this::checkConnectionHealth,
                HEARTBEAT_INTERVAL + 10, HEARTBEAT_INTERVAL + 10, TimeUnit.SECONDS);
    }

    public boolean isOpen() {
        WebSocket webSocket = this.wsRef.get();
        return webSocket != null && !webSocket.isClosed();
    }

    /**
     * 发送心跳消息 - 使用新的二进制协议
     */
    private void sendHeartbeat() {
        if (isOpen()) {
            try {
                Buffer heartbeatBuffer = buildHeartbeatMessage();
                getWebSocket().write(heartbeatBuffer);
                Log.d(TAG, "发送心跳消息");
            } catch (Exception e) {
                Log.e(TAG, "发送心跳消息失败", e);
                // 心跳发送失败可能表示连接有问题，触发重连检查
                triggerReconnectIfNeeded();
            }
        } else if (shouldReconnect && !isReconnecting) {
            // 如果连接已断开且需要重连，触发重连
            triggerReconnectIfNeeded();
        }
    }

    /**
     * 检查连接健康状态
     */
    private void checkConnectionHealth() {
        if (!isOpen()) {
            if (shouldReconnect && !isReconnecting) {
                Log.w(TAG, "检测到连接断开，准备重连");
                triggerReconnectIfNeeded();
            }
            return;
        }

        long currentTime = System.currentTimeMillis();
        long timeSinceLastHeartbeat = currentTime - lastHeartbeat;

        // 如果超过2分钟没有收到任何消息，认为连接可能有问题
        if (timeSinceLastHeartbeat > CONNECTION_TIMEOUT) {
            Log.w(TAG, "连接超时，最后心跳时间: " + (timeSinceLastHeartbeat / 1000) + "秒前");
            if (shouldReconnect && !isReconnecting) {
                // 主动关闭可能已经僵死的连接
                try {
                    getWebSocket().close();
                } catch (Exception e) {
                    Log.e(TAG, "关闭僵死连接失败", e);
                }
                triggerReconnectIfNeeded();
            }
        }
    }

    /**
     * 构建心跳消息
     */
    private Buffer buildHeartbeatMessage() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(MSG_TYPE_HEARTBEAT);
        buffer.flip();
        return Buffer.buffer(buffer.array());
    }

    public WebSocket getWebSocket() {
        return this.wsRef.get();
    }

    /**
     * 设置网络监听器
     */
    public void setNetworkListener(NetworkListener listener) {
        this.listener = listener;
    }

    /**
     * 发送客户端消息 - 使用新的二进制协议
     */
    public void sendClientMessage(byte[] message) {
        if (isOpen() && message != null) {
            try {
                Buffer clientBuffer = buildClientMessage(message);
                getWebSocket().write(clientBuffer);
                Log.d(TAG, "发送客户端消息，长度: " + message.length);
            } catch (Exception e) {
                Log.e(TAG, "发送客户端消息失败", e);
            }
        }
    }

    public void sendClientMessage(int type, MessageLite messageLite) {
        this.sendClientMessage(Utils.encode(type, messageLite).getBytes());
    }

    /**
     * 请求房间成员数量
     */
    public void requestRoomMemberCount() {
        if (isOpen()) {
            try {
                Buffer requestBuffer = buildRoomMemberCountRequest();
                getWebSocket().write(requestBuffer);
                Log.d(TAG, "请求房间成员数量");
            } catch (Exception e) {
                Log.e(TAG, "请求房间成员数量失败", e);
            }
        }
    }

    /**
     * 构建客户端消息
     */
    private Buffer buildClientMessage(byte[] originalMessage) {
        ByteBuffer buffer = ByteBuffer.allocate(4 + originalMessage.length);
        buffer.putInt(MSG_TYPE_CLIENT_MESSAGE);
        buffer.put(originalMessage);
        buffer.flip();
        return Buffer.buffer(buffer.array());
    }

    /**
     * 构建房间成员数量请求消息
     */
    private Buffer buildRoomMemberCountRequest() {
        ByteBuffer buffer = ByteBuffer.allocate(4 + 1);
        buffer.putInt(MSG_TYPE_ROOM_NOTIFICATION);
        buffer.put(ROOM_EVENT_ROOM_MEMBER_COUNT);
        buffer.flip();
        return Buffer.buffer(buffer.array());
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    /**
     * 触发重连（如果需要）
     */
    private void triggerReconnectIfNeeded() {
        if (!shouldReconnect || isReconnecting) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        // 避免重连过于频繁，至少间隔1秒
        if (currentTime - lastReconnectTime < 1000) {
            return;
        }

        lastReconnectTime = currentTime;
        scheduleReconnect();
    }

    /**
     * 安排重连任务
     */
    private void scheduleReconnect() {
        if (isReconnecting || !shouldReconnect) {
            return;
        }

        isReconnecting = true;
        reconnectAttempts++;

        // 计算重连延迟（指数退避算法）
        long delay = calculateReconnectDelay(reconnectAttempts);

        Log.i(TAG, "安排第 " + reconnectAttempts + " 次重连，延迟: " + delay + "ms");

        if (listener != null) {
            listener.onReconnectStarted(reconnectAttempts);
        }

        reconnectScheduler.schedule(() -> {
            try {
                performReconnect();
            } catch (Exception e) {
                handleReconnectFailure(e);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * 执行重连
     */
    private void performReconnect() {
        if (!shouldReconnect) {
            isReconnecting = false;
            return;
        }

        Log.i(TAG, "执行第 " + reconnectAttempts + " 次重连");

        try {
            // 清理旧连接
            WebSocket oldSocket = wsRef.get();
            if (oldSocket != null && !oldSocket.isClosed()) {
                try {
                    oldSocket.close();
                } catch (Exception e) {
                    Log.w(TAG, "关闭旧连接失败", e);
                }
            }
            wsRef.set(null);

            // 尝试建立新连接
            connectInternal();

            // 重连成功
            Log.i(TAG, "重连成功，尝试次数: " + reconnectAttempts);
            int attempts = reconnectAttempts;
            resetReconnectState();

            if (listener != null) {
                listener.onReconnectSuccess(attempts);
            }

        } catch (Throwable e) {
            handleReconnectFailure(e);
        }
    }

    /**
     * 处理重连失败
     */
    private void handleReconnectFailure(Throwable error) {
        Log.w(TAG, "第 " + reconnectAttempts + " 次重连失败", error);

        if (listener != null) {
            listener.onReconnectFailed(reconnectAttempts, error);
        }

        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            // 达到最大重连次数，放弃重连
            Log.e(TAG, "达到最大重连次数 " + MAX_RECONNECT_ATTEMPTS + "，放弃重连");
            shouldReconnect = false;
            isReconnecting = false;

            if (listener != null) {
                listener.onReconnectGiveUp(reconnectAttempts);
            }
        } else {
            // 继续重连
            isReconnecting = false;
            scheduleReconnect();
        }
    }

    /**
     * 计算重连延迟（指数退避算法）
     */
    private long calculateReconnectDelay(int attempt) {
        long delay = (long) (INITIAL_RECONNECT_DELAY * Math.pow(RECONNECT_BACKOFF_MULTIPLIER, attempt - 1));
        return Math.min(delay, MAX_RECONNECT_DELAY);
    }

    /**
     * 重置重连状态
     */
    private void resetReconnectState() {
        isReconnecting = false;
        reconnectAttempts = 0;
        lastReconnectTime = 0;
    }

    /**
     * 内部连接方法
     */
    private void connectInternal() throws Throwable {
        Exchanger<AsyncResult<WebSocket>> success = new Exchanger<>();
        String url = AppConfig.WEBSOCKET_URL + "?roomId=" + this.roomId + "&deviceId=" + this.deviceId;
        Log.i(TAG, "connect url" + url);
        Constant.VERTX.createHttpClient().webSocket(AppConfig.WEBSOCKET_PORT, AppConfig.WEBSOCKET_HOST, url, result -> {
            try {
                success.exchange(result);
            } catch (Exception e) {
                Log.e(TAG, "连接错误", e);
            }
        });
        AsyncResult<WebSocket> result = success.exchange(null);
        if (result.succeeded()) {
            lastHeartbeat = System.currentTimeMillis();
            this.wsRef.set(result.result());
            bindHandler(getWebSocket());
            Log.i(TAG, "WebSocket连接成功，房间ID: " + roomId);
        } else {
            throw result.cause();
        }
    }

    /**
     * 连接到WebSocket服务器，支持房间ID参数
     */
    public void connect() throws Throwable {
        shouldReconnect = true;
        resetReconnectState();
        connectInternal();
    }

    /**
     * 绑定WebSocket消息处理器 - 支持新的二进制协议
     */
    private void bindHandler(WebSocket webSocket) {
        webSocket.closeHandler(v -> {
            Log.i(TAG, "WebSocket连接关闭");
            if (listener != null) {
                listener.onConnectionClosed();
            }
            // 连接关闭时触发重连
            triggerReconnectIfNeeded();
        });

        webSocket.exceptionHandler(exception -> {
            Log.e(TAG, "WebSocket连接异常", exception);
            if (listener != null) {
                listener.onConnectionError(exception);
            }
            // 连接异常时触发重连
            triggerReconnectIfNeeded();
        });

        webSocket.handler(buffer -> {
            try {
                handleBinaryMessage(buffer.getBytes());
            } catch (Exception ex) {
                Log.e(TAG, "处理消息错误", ex);
            }
        });
    }

    /**
     * 处理接收到的二进制消息
     */
    private void handleBinaryMessage(byte[] data) {
        if (data.length < 4) {
            Log.w(TAG, "消息长度不足，忽略");
            return;
        }

        // 更新心跳时间
        lastHeartbeat = System.currentTimeMillis();

        ByteBuffer buffer = ByteBuffer.wrap(data);
        int messageType = buffer.getInt();

        switch (messageType) {
            case MSG_TYPE_HEARTBEAT:
                handleHeartbeatResponse();
                break;

            case MSG_TYPE_ROOM_NOTIFICATION:
                handleRoomNotification(buffer);
                break;

            case MSG_TYPE_CLIENT_MESSAGE:
                handleClientMessage(buffer);
                break;

            default:
                Log.w(TAG, "未知消息类型: " + messageType);
                break;
        }
    }

    /**
     * 处理心跳响应
     */
    private void handleHeartbeatResponse() {
        Log.d(TAG, "收到心跳响应");
    }

    /**
     * 处理房间通知消息
     */
    private void handleRoomNotification(ByteBuffer buffer) {
        if (buffer.remaining() < 1) {
            Log.w(TAG, "房间通知消息格式错误");
            return;
        }

        byte eventType = buffer.get();

        switch (eventType) {
            case ROOM_EVENT_CLIENT_JOINED:
                handleClientJoinedNotification(buffer);
                break;

            case ROOM_EVENT_CLIENT_LEFT:
                handleClientLeftNotification(buffer);
                break;

            case ROOM_EVENT_CLIENT_ERROR:
                handleClientErrorNotification(buffer);
                break;

            case ROOM_EVENT_ROOM_MEMBER_COUNT:
                handleRoomMemberCountResponse(buffer);
                break;

            default:
                Log.w(TAG, "未知房间通知事件类型: " + eventType);
                break;
        }
    }

    /**
     * 处理客户端加入通知
     */
    private void handleClientJoinedNotification(ByteBuffer buffer) {
        String sessionId = extractSessionId(buffer);
        if (sessionId != null && listener != null) {
            Log.i(TAG, "客户端加入: " + sessionId);
            listener.onClientJoined(sessionId);
        }
    }

    /**
     * 处理客户端离开通知
     */
    private void handleClientLeftNotification(ByteBuffer buffer) {
        String sessionId = extractSessionId(buffer);
        if (sessionId != null && listener != null) {
            Log.i(TAG, "客户端离开: " + sessionId);
            listener.onClientLeft(sessionId);
        }
    }

    /**
     * 处理客户端错误通知
     */
    private void handleClientErrorNotification(ByteBuffer buffer) {
        String sessionId = extractSessionId(buffer);
        if (sessionId != null && listener != null) {
            Log.w(TAG, "客户端错误: " + sessionId);
            listener.onClientError(sessionId);
        }
    }

    /**
     * 处理房间成员数量响应
     */
    private void handleRoomMemberCountResponse(ByteBuffer buffer) {
        if (buffer.remaining() < 4) {
            Log.w(TAG, "房间成员数量响应格式错误");
            return;
        }

        int memberCount = buffer.getInt();
        Log.i(TAG, "房间成员数量: " + memberCount);

        if (listener != null) {
            listener.onRoomMemberCount(memberCount);
        }
    }

    /**
     * 处理客户端消息
     */
    private void handleClientMessage(ByteBuffer buffer) {
        if (buffer.remaining() > 0) {
            byte[] message = new byte[buffer.remaining()];
            buffer.get(message);

            Log.d(TAG, "收到客户端消息，长度: " + message.length);

            if (listener != null) {
                Log.i(TAG, "onClientMessage: ");
                listener.onClientMessage(message);
            }else{
                Log.w(TAG, "listener is null");
            }
        }
    }

    /**
     * 从缓冲区中提取会话ID
     */
    private String extractSessionId(ByteBuffer buffer) {
        if (buffer.remaining() < 4) {
            return null;
        }

        int sessionIdLength = buffer.getInt();
        if (buffer.remaining() < sessionIdLength) {
            return null;
        }

        byte[] sessionIdBytes = new byte[sessionIdLength];
        buffer.get(sessionIdBytes);
        return new String(sessionIdBytes);
    }

    /**
     * 断开WebSocket连接并清理资源
     */
    public void disconnect() {
        Log.i(TAG, "断开WebSocket连接");

        // 禁用自动重连
        shouldReconnect = false;
        isReconnecting = false;

        // 停止心跳调度器
        if (heartbeatScheduler != null && !heartbeatScheduler.isShutdown()) {
            heartbeatScheduler.shutdown();
            try {
                if (!heartbeatScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    heartbeatScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                heartbeatScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // 停止重连调度器
        if (reconnectScheduler != null && !reconnectScheduler.isShutdown()) {
            reconnectScheduler.shutdown();
            try {
                if (!reconnectScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    reconnectScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                reconnectScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // 关闭WebSocket连接
        if (isOpen()) {
            this.wsRef.get().close();
        }

        // 清理引用
        this.wsRef.set(null);
        // this.listener = null;

        // 重置重连状态
        resetReconnectState();
    }

    /**
     * 启用自动重连
     */
    public void enableAutoReconnect() {
        shouldReconnect = true;
        Log.i(TAG, "已启用自动重连");
    }

    /**
     * 禁用自动重连
     */
    public void disableAutoReconnect() {
        shouldReconnect = false;
        isReconnecting = false;
        Log.i(TAG, "已禁用自动重连");
    }

    /**
     * 手动重连
     */
    public void reconnect() {
        Log.i(TAG, "手动重连");
        shouldReconnect = true;
        resetReconnectState();
        triggerReconnectIfNeeded();
    }

    /**
     * 获取重连状态
     */
    public boolean isReconnecting() {
        return isReconnecting;
    }

    /**
     * 获取重连尝试次数
     */
    public int getReconnectAttempts() {
        return reconnectAttempts;
    }

    /**
     * 是否启用了自动重连
     */
    public boolean isAutoReconnectEnabled() {
        return shouldReconnect;
    }

    /**
     * 获取当前房间ID
     */
    public String getRoomId() {
        return roomId;
    }

    /**
     * 设置房间ID
     */
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    /**
     * 获取设备ID
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * 检查连接是否健康（基于心跳时间）
     */
    public boolean isConnectionHealthy() {
        if (!isOpen()) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long timeSinceLastHeartbeat = currentTime - lastHeartbeat;
        // 如果超过2分钟没有心跳，认为连接不健康
        return timeSinceLastHeartbeat < 120000;
    }
}
