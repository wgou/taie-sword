//package io.renren.modules.app.web.ws;
//
//import com.alibaba.fastjson.JSONObject;
//import com.google.protobuf.MessageOrBuilder;
//import io.renren.common.constant.Constant;
//import io.renren.modules.app.common.Utils;
//import io.renren.modules.app.entity.Device;
//import io.renren.modules.app.message.Context;
//import io.renren.modules.app.message.MessageHandler;
//import com.ghost.frc.proto.Message;
//import io.renren.modules.app.service.DeviceService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.context.ApplicationContext;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.BinaryMessage;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.BinaryWebSocketHandler;
//
//import javax.annotation.Resource;
//import java.net.InetSocketAddress;
//import java.net.URI;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
///**
// * ws处理器
// */
//@Component
//@Slf4j
//public class WebSocketHandler_old extends BinaryWebSocketHandler implements InitializingBean {
//    @Resource
//    private ApplicationContext applicationContext;
//    @Resource
//    private DeviceService deviceService;
//
//    private String handlerKey(int source, int type) {
//        return String.format("%s:%s", source, type);
//    }
//
//    private final ConcurrentHashMap<String, WebSocketSession> deviceMap = new ConcurrentHashMap<>();
//    private final ConcurrentHashMap<String, List<WebSocketSession>> monitorMap = new ConcurrentHashMap<>();
//
//
//    private ConcurrentHashMap<String, List<MessageHandler>> handlerMap = new ConcurrentHashMap<>();
//
//
//    private void healthCheck() {
//        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
//        scheduledExecutorService.scheduleWithFixedDelay(() -> {
//            Iterator<Map.Entry<String, WebSocketSession>> iterator = deviceMap.entrySet().iterator();
//            while (iterator.hasNext()) {
//                Map.Entry<String, WebSocketSession> entry = iterator.next();
//                WebSocketSession webSocketSession = entry.getValue();
//                if (webSocketSession == null || !webSocketSession.isOpen()) {
//                    log.warn("{} - 清理device死链接", entry.getKey());
//                    iterator.remove();
//                }
//            }
//
//
//            for (Map.Entry<String, List<WebSocketSession>> entry : monitorMap.entrySet()) {
//                List<WebSocketSession> list = entry.getValue();
//                if (CollectionUtils.isNotEmpty(list)) {
//                    Iterator<WebSocketSession> monitorListIt = list.iterator();
//                    while (monitorListIt.hasNext()) {
//                        WebSocketSession wss = monitorListIt.next();
//                        if (wss == null || !wss.isOpen()) {
//                            log.warn("{} - 清理monitor死链接", entry.getKey());
//                            monitorListIt.remove();
//                        }
//
//                    }
//                }
//            }
//
//        }, 20, 5, TimeUnit.SECONDS);
//    }
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        healthCheck();
//        Collection<MessageHandler> values = applicationContext.getBeansOfType(MessageHandler.class).values();
//        for (MessageHandler messageHandler : values) {
//            int type = messageHandler.type();
//            String key = handlerKey(messageHandler.source(), type);
//            List<MessageHandler> messageHandlers = handlerMap.computeIfAbsent(key, k -> new ArrayList<>());
//            messageHandlers.add(messageHandler);
//        }
//
//
//    }
//
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        String id = session.getId();
//        InetSocketAddress remoteAddress = session.getRemoteAddress();
//        URI uri = session.getUri();
//        String query = uri.getQuery();
//
//        log.info("{} - afterConnectionEstablished:{} - {} - {}", id, remoteAddress.getHostName(), remoteAddress.getHostString(), query);
//
////        String[] params = query.split("&");
////        JSONObject jsonObject = new JSONObject();
////        for (String param : params) {
////            String[] _param = param.split("=");
////            jsonObject.put(_param[0], "");
////            if (_param.length > 1) {
////                jsonObject.put(_param[0], _param[1]);
////            }
////        }
////
////        String deviceId = jsonObject.getString("deviceId");
////        if(StringUtils.isEmpty(deviceId)){
////            log.info("{} - deviceId is null", query);
////            session.close();
////        }
////
////        Device device = deviceService.findByDeviceId(deviceId);
////        if(device == null){
////            log.info("{} - 设备还未注册,不允许长连接", deviceId);
////            session.close();
////        }
////        log.info("{} - 连接成功", deviceId);
////        WebSocketSession webSocketSession = deviceMap.get(deviceId);
////        if(webSocketSession != null){
////            log.info("关闭老的链接 - {}", deviceId);
////            webSocketSession.close();
////        }
////        deviceMap.put(deviceId, session);
//    }
//
//    @Override
//    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
//        byte[] bytes = message.getPayload().array();
//        try {
//            //序列化
//            Message.WsMessage wsMessage = Message.WsMessage.parseFrom(bytes);
//            //decode
//            //获取身份
//            int source = wsMessage.getSource();
//            int type = wsMessage.getType();
//            String key = handlerKey(source, type);
//            List<MessageHandler> messageHandlers = handlerMap.get(key);
//            if (CollectionUtils.isEmpty(messageHandlers)) {
//                log.warn("未设置消息处理器:{}", key);
//            } else {
//                byte[] body = wsMessage.getBody().toByteArray();
//                //解压?
//                byte[] _body = Utils.decompress(body);
//
//                log.info("收到新消息:解压前大小:{} byte, 解缩后大小:{} byte", _body.length, body.length);
//                MessageOrBuilder _message = null;
//                //反序列化
//                switch (type) {
//                    case Constant.MessageType.android_online:
//                        _message = Message.AndroidOnline.parseFrom(_body);
//                        break;
//                    case Constant.MessageType.screen_info:
//                        _message = Message.ScreenInfo.parseFrom(_body);
//                        break;
//                    case Constant.MessageType.touch_req:
//                        _message = Message.TouchReq.parseFrom(_body);
//                        break;
//                    case Constant.MessageType.scroll_req:
//                        _message = Message.ScrollReq.parseFrom(_body);
//                        break;
//                    case Constant.MessageType.back_req:
//                        _message = Message.BackReq.parseFrom(_body);
//                        break;
//                    case Constant.MessageType.home_req:
//                        _message = Message.HomeReq.parseFrom(_body);
//                        break;
//                    case Constant.MessageType.recents_req:
//                        _message = Message.RecentsReq.parseFrom(_body);
//                        break;
//                    case Constant.MessageType.monitor_online:
//                        _message = Message.MonitorOnline.parseFrom(_body);
//                        break;
//                    case Constant.MessageType.input_text:
//                        _message = Message.InputText.parseFrom(_body);
//                        break;
//                    case Constant.MessageType.install_app_req:
//                        _message = Message.InstallAppReq.parseFrom(_body);
//                        break;
//                    case Constant.MessageType.install_app_resp:
//                        _message = Message.InstallAppResp.parseFrom(_body);
//                        break;
//                    case Constant.MessageType.start_app_req:
//                        _message = Message.StartAppReq.parseFrom(_body);
//                        break;
//                    case Constant.MessageType.slide_req:
//                        _message = Message.SlideReq.parseFrom(_body);
//                        break;
//                    case Constant.MessageType.screen_req:
//                        _message = Message.ScreenReq.parseFrom(_body);
//                        break;
//                    case Constant.MessageType.ping:
//                        _message = Message.Ping.parseFrom(_body);
//                        break;
//                    case Constant.MessageType.lock_screen:
//                        _message = Message.LockScreen.parseFrom(_body);
//                        break;
//                    case Constant.MessageType.un_lock_screen_req:
//                        _message = Message.UnLockScreenReq.parseFrom(_body);
//                        break;
//                    default:
//                        throw new RuntimeException("消息转换错误!");
//
//                }
//                Class<? extends MessageOrBuilder> messageType = _message.getClass();
//
//                if (!(_message instanceof Message.ScreenInfo)) {
//                    String json = Utils.protoToJson(_message);
//                    log.info("receive:{} | {}", json, messageType);
//                }
//
//                Context context = new Context(deviceMap, monitorMap, session);
//                for (MessageHandler handlerHandler : messageHandlers) {
//                    try {
//                        handlerHandler.handler(_message, context);
//                    } catch (Exception ex) {
//                        log.warn("handler error", ex);
//                    }
//                }
//            }
//
//
//        } catch (Exception e) {
//            log.warn("处理错误:", e);
//        }
//
//    }
//
//
//    @Override
//    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
//        log.warn("handleTransportError");
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        log.warn("afterConnectionClosed");
//    }
//
//
//}
