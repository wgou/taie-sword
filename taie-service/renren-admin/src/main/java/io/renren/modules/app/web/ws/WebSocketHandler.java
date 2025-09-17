package io.renren.modules.app.web.ws;

import io.renren.common.constant.Constant;
import io.renren.modules.app.common.Utils;
import io.renren.modules.app.message.proto.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class WebSocketHandler extends BinaryWebSocketHandler implements InitializingBean {

    // 房间管理：房间ID -> 客户端会话集合
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    // 会话管理：会话ID -> 房间ID
    private final ConcurrentHashMap<String, String> sessionRoomMap = new ConcurrentHashMap<>();

    // 会话管理：会话ID -> 最后心跳时间
    private final ConcurrentHashMap<String, Long> sessionHeartbeatMap = new ConcurrentHashMap<>();

    // 心跳检测定时器
    private ScheduledExecutorService heartbeatScheduler;

    // 心跳间隔（秒）
    private static final int HEARTBEAT_INTERVAL = 30;
    // 心跳超时时间（秒）
    private static final int HEARTBEAT_TIMEOUT = 60;

    // 消息类型常量 - 前4字节用于标识消息类型
    private static final int MSG_TYPE_ROOM_NOTIFICATION = 0x00000001;  // 房间通知消息
    private static final int MSG_TYPE_CLIENT_MESSAGE = 0x00000002;     // 客户端消息
    private static final int MSG_TYPE_HEARTBEAT = 0x00000003;          // 心跳消息

    // 房间通知子类型
    private static final byte ROOM_EVENT_CLIENT_JOINED = 0x01;
    private static final byte ROOM_EVENT_CLIENT_LEFT = 0x02;
    private static final byte ROOM_EVENT_CLIENT_ERROR = 0x03;
    private static final byte ROOM_EVENT_ROOM_MEMBER_COUNT = 0x04;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化心跳检测定时器
        heartbeatScheduler = Executors.newScheduledThreadPool(1);
        heartbeatScheduler.scheduleAtFixedRate(this::checkHeartbeat, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
        log.info("WebSocketHandler initialized with heartbeat checking");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());

        // 从URL参数中获取房间ID
        String roomId = getRoomIdFromSession(session);
        if (roomId == null || roomId.trim().isEmpty()) {
            log.warn("Room ID not found in session: {}", session.getId());
            session.close(CloseStatus.BAD_DATA.withReason("Room ID is required"));
            return;
        }

        // 将会话加入房间
        joinRoom(roomId, session);

        // 记录心跳时间
        sessionHeartbeatMap.put(session.getId(), System.currentTimeMillis());

        // 通知房间内其他成员有新客户端加入
        notifyRoomMembers(roomId, session, ROOM_EVENT_CLIENT_JOINED);
        //通知客服端数量
        notifyRoomMemberCount(roomId);

        log.info("Client {} joined room: {}", session.getId(), roomId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {}, status: {}", session.getId(), status);

        String roomId = sessionRoomMap.get(session.getId());
        if (roomId != null) {
            // 从房间移除会话
            leaveRoom(roomId, session);

            // 通知房间内其他成员有客户端离开
            notifyRoomMembers(roomId, session, ROOM_EVENT_CLIENT_LEFT);
            notifyRoomMemberCount(roomId);
        }

        // 清理会话相关数据
        sessionRoomMap.remove(session.getId());
        sessionHeartbeatMap.remove(session.getId());

        log.info("Client {} left room: {}", session.getId(), roomId);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        String sessionId = session.getId();
        String roomId = sessionRoomMap.get(sessionId);

        if (roomId == null) {
            log.warn("Session {} not in any room", sessionId);
            return;
        }

        // 更新心跳时间
        sessionHeartbeatMap.put(sessionId, System.currentTimeMillis());

        ByteBuffer payload = message.getPayload();

        // 解析消息类型
        int messageType = parseMessageType(payload);

        if (messageType == MSG_TYPE_HEARTBEAT) {
            // 处理心跳消息
            handleHeartbeatMessage(session);
            return;
        } else if (messageType == MSG_TYPE_ROOM_NOTIFICATION) {
            // 处理房间通知请求
            handleRoomNotificationRequest(session, roomId, payload);
            return;
        }

        // 提取原始消息内容（跳过前4字节的类型标识）
        byte[] originalContent = new byte[payload.remaining() - 4];
        payload.position(4); // 跳过消息类型
        payload.get(originalContent);
        //解析 protobuf
        try {
            Message.WsMessage wsMessage = Message.WsMessage.parseFrom(originalContent);
            int source = wsMessage.getSource();
            log.info("source:{}", source);
            //解压缩
            byte[] body = Utils.decompress(wsMessage.getBody().toByteArray());
            switch (wsMessage.getType()) {
                case Constant.MessageType.screen_info: {
                    Message.ScreenInfo screenInfo = Message.ScreenInfo.parseFrom(body);
                    log.info("ScreenInfo: {}", Utils.protoToJson(screenInfo));
                    break;
                }
                case Constant.MessageType.touch_req: {
                    Message.TouchReq touchReq = Message.TouchReq.parseFrom(body);
                    log.info("TouchReq: {}", Utils.protoToJson(touchReq));
                    break;
                }
                case Constant.MessageType.input_text: {
                    Message.InputText inputText = Message.InputText.parseFrom(body);
                    log.info("InputText: {}", Utils.protoToJson(inputText));
                    break;
                }
                default: {
                    log.warn("{} - 未知的消息类型", wsMessage.getType());
                    break;
                }
            }
        } catch (Exception ex) {
            log.warn("转发解析错误:", ex);
        }
        //解析 protobuf

        // 转发消息给房间内其他客户端
        forwardBinaryMessageToRoom(roomId, session, originalContent);

        log.info("Binary message forwarded from {} to room {}, size: {} bytes",
                sessionId, roomId, originalContent.length);
    }


    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        // 处理客户端的pong响应
        sessionHeartbeatMap.put(session.getId(), System.currentTimeMillis());
        log.debug("Received pong from session: {}", session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session: {}", session.getId(), exception);

        // 处理传输错误，清理资源
        String roomId = sessionRoomMap.get(session.getId());
        if (roomId != null) {
            leaveRoom(roomId, session);
            notifyRoomMembers(roomId, session, ROOM_EVENT_CLIENT_ERROR);
            notifyRoomMemberCount(roomId);
        }

        sessionRoomMap.remove(session.getId());
        sessionHeartbeatMap.remove(session.getId());
    }

    /**
     * 从会话中提取房间ID
     */
    private String getRoomIdFromSession(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri != null && uri.getQuery() != null) {
            String query = uri.getQuery();
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && "roomId".equals(keyValue[0])) {
                    return keyValue[1];
                }
            }
        }
        return null;
    }

    /**
     * 解析消息类型（前4字节）
     */
    private int parseMessageType(ByteBuffer payload) {
        if (payload.remaining() < 4) {
            return -1;
        }
        payload.mark();
        int messageType = payload.getInt();
        payload.reset();
        return messageType;
    }

    /**
     * 构建房间通知消息
     */
    private BinaryMessage buildRoomNotificationMessage(byte eventType, String sessionId) {
        byte[] sessionIdBytes = sessionId.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(4 + 1 + 4 + sessionIdBytes.length);

        buffer.putInt(MSG_TYPE_ROOM_NOTIFICATION);  // 消息类型：房间通知
        buffer.put(eventType);                      // 事件子类型
        buffer.putInt(sessionIdBytes.length);       // 会话ID长度
        buffer.put(sessionIdBytes);                 // 会话ID

        buffer.flip();
        return new BinaryMessage(buffer);
    }

    /**
     * 构建心跳响应消息
     */
    private BinaryMessage buildHeartbeatResponse() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(MSG_TYPE_HEARTBEAT);
        buffer.flip();
        return new BinaryMessage(buffer);
    }

    /**
     * 构建客户端消息（用于转发）
     */
    private BinaryMessage buildClientMessage(byte[] originalPayload) {
        ByteBuffer buffer = ByteBuffer.allocate(4 + originalPayload.length);
        buffer.putInt(MSG_TYPE_CLIENT_MESSAGE);     // 消息类型：客户端消息
        buffer.put(originalPayload);               // 原始消息内容
        buffer.flip();
        return new BinaryMessage(buffer);
    }

    /**
     * 构建房间成员数量响应消息
     */
    private BinaryMessage buildRoomMemberCountResponse(int memberCount) {
        ByteBuffer buffer = ByteBuffer.allocate(4 + 1 + 4);
        buffer.putInt(MSG_TYPE_ROOM_NOTIFICATION);      // 消息类型：房间通知
        buffer.put(ROOM_EVENT_ROOM_MEMBER_COUNT);       // 事件类型：房间成员数量
        buffer.putInt(memberCount);                     // 成员数量
        buffer.flip();
        return new BinaryMessage(buffer);
    }

    /**
     * 将会话加入房间
     */
    private void joinRoom(String roomId, WebSocketSession session) {
        rooms.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(session);
        sessionRoomMap.put(session.getId(), roomId);
    }

    /**
     * 将会话从房间移除
     */
    private void leaveRoom(String roomId, WebSocketSession session) {
        CopyOnWriteArraySet<WebSocketSession> roomSessions = rooms.get(roomId);
        if (roomSessions != null) {
            roomSessions.remove(session);
            if (roomSessions.isEmpty()) {
                rooms.remove(roomId);
                log.info("Room {} is empty and removed", roomId);
            }
        }
    }

    /**
     * 转发二进制消息给房间内其他客户端
     */
    private void forwardBinaryMessageToRoom(String roomId, WebSocketSession senderSession, byte[] originalContent) {
        CopyOnWriteArraySet<WebSocketSession> roomSessions = rooms.get(roomId);
        if (roomSessions == null || roomSessions.isEmpty()) {
            return;
        }

        roomSessions.parallelStream()
                .filter(session -> !session.getId().equals(senderSession.getId())) // 不转发给发送者
                .filter(WebSocketSession::isOpen) // 只转发给开放的会话
                .forEach(session -> {
                    try {
                        // 构建带有消息类型标识的转发消息
                        BinaryMessage forwardMessage = buildClientMessage(originalContent);
                        session.sendMessage(forwardMessage);
                    } catch (IOException e) {
                        log.error("Failed to forward message to session: {}", session.getId(), e);
                        // 发送失败的会话可能已断开，从房间移除
                        removeDeadSession(roomId, session);
                    }
                });
    }

    /**
     * 通知房间成员客户端状态变化
     */
    private void notifyRoomMembers(String roomId, WebSocketSession targetSession, byte eventType) {
        CopyOnWriteArraySet<WebSocketSession> roomSessions = rooms.get(roomId);
        if (roomSessions == null || roomSessions.isEmpty()) {
            return;
        }

        roomSessions.parallelStream()
                .filter(session -> !session.getId().equals(targetSession.getId())) // 不通知目标会话自己
                .filter(WebSocketSession::isOpen)
                .forEach(session -> {
                    try {
                        // 构建房间通知消息
                        BinaryMessage notification = buildRoomNotificationMessage(eventType, targetSession.getId());
                        session.sendMessage(notification);
                    } catch (IOException e) {
                        log.error("Failed to send notification to session: {}", session.getId(), e);
                        removeDeadSession(roomId, session);
                    }
                });
    }

    /**
     * 通知房间成员客户端状态变化
     */
    private void notifyRoomMemberCount(String roomId) {
        CopyOnWriteArraySet<WebSocketSession> roomSessions = rooms.get(roomId);
        if (roomSessions == null || roomSessions.isEmpty()) {
            return;
        }

        roomSessions.parallelStream()
//                .filter(session -> !session.getId().equals(targetSession.getId())) // 不通知目标会话自己
                .filter(WebSocketSession::isOpen)
                .forEach(session -> {
                    try {
                        // 构建房间通知消息
                        BinaryMessage binaryMessage = buildRoomMemberCountResponse(roomSessions.size());
                        session.sendMessage(binaryMessage);
                    } catch (IOException e) {
                        log.error("Failed to send notification to session: {}", session.getId(), e);
                        removeDeadSession(roomId, session);
                    }
                });
    }


    /**
     * 处理心跳消息
     */
    private void handleHeartbeatMessage(WebSocketSession session) {
        try {
            // 回复心跳响应
            BinaryMessage heartbeatResponse = buildHeartbeatResponse();
            session.sendMessage(heartbeatResponse);
            log.debug("Sent heartbeat response to session: {}", session.getId());
        } catch (IOException e) {
            log.error("Failed to send heartbeat response to session: {}", session.getId(), e);
        }
    }

    /**
     * 处理房间通知请求
     */
    private void handleRoomNotificationRequest(WebSocketSession session, String roomId, ByteBuffer payload) {
        if (payload.remaining() < 5) { // 4字节类型 + 1字节事件类型
            log.warn("Invalid room notification request from session: {}", session.getId());
            return;
        }

        payload.position(4); // 跳过消息类型
        byte eventType = payload.get();

        switch (eventType) {
            case ROOM_EVENT_ROOM_MEMBER_COUNT:
                handleRoomMemberCountRequest(session, roomId);
                break;
            default:
                log.warn("Unknown room notification event type {} from session: {}", eventType, session.getId());
                break;
        }
    }

    /**
     * 处理房间成员数量查询请求
     */
    private void handleRoomMemberCountRequest(WebSocketSession session, String roomId) {
        try {
            int memberCount = getRoomSessionCount(roomId);
            BinaryMessage response = buildRoomMemberCountResponse(memberCount);
            session.sendMessage(response);
            log.debug("Sent room member count {} to session: {}", memberCount, session.getId());
        } catch (IOException e) {
            log.error("Failed to send room member count to session: {}", session.getId(), e);
        }
    }

    /**
     * 心跳检测任务
     */
    private void checkHeartbeat() {
        long currentTime = System.currentTimeMillis();
        long timeoutThreshold = currentTime - (HEARTBEAT_TIMEOUT * 1000L);

        sessionHeartbeatMap.entrySet().removeIf(entry -> {
            String sessionId = entry.getKey();
            long lastHeartbeat = entry.getValue();

            if (lastHeartbeat < timeoutThreshold) {
                log.warn("Session {} heartbeat timeout, removing", sessionId);

                // 查找并关闭超时的会话
                String roomId = sessionRoomMap.get(sessionId);
                if (roomId != null) {
                    CopyOnWriteArraySet<WebSocketSession> roomSessions = rooms.get(roomId);
                    if (roomSessions != null) {
                        roomSessions.removeIf(session -> {
                            if (session.getId().equals(sessionId)) {
                                try {
                                    if (session.isOpen()) {
                                        session.close(CloseStatus.SESSION_NOT_RELIABLE.withReason("Heartbeat timeout"));
                                    }
                                } catch (IOException e) {
                                    log.error("Failed to close timeout session: {}", sessionId, e);
                                }
                                return true;
                            }
                            return false;
                        });
                    }
                    sessionRoomMap.remove(sessionId);
                }
                return true;
            }
            return false;
        });
    }

    /**
     * 移除失效的会话
     */
    private void removeDeadSession(String roomId, WebSocketSession deadSession) {
        CopyOnWriteArraySet<WebSocketSession> roomSessions = rooms.get(roomId);
        if (roomSessions != null) {
            roomSessions.remove(deadSession);
            if (roomSessions.isEmpty()) {
                rooms.remove(roomId);
            }
        }
        sessionRoomMap.remove(deadSession.getId());
        sessionHeartbeatMap.remove(deadSession.getId());

        try {
            if (deadSession.isOpen()) {
                deadSession.close();
            }
        } catch (IOException e) {
            log.error("Failed to close dead session: {}", deadSession.getId(), e);
        }
    }

    /**
     * 获取房间信息（用于监控）
     */
    public int getRoomCount() {
        return rooms.size();
    }

    public int getTotalSessionCount() {
        return sessionRoomMap.size();
    }

    public int getRoomSessionCount(String roomId) {
        CopyOnWriteArraySet<WebSocketSession> roomSessions = rooms.get(roomId);
        return roomSessions != null ? roomSessions.size() : 0;
    }
}
