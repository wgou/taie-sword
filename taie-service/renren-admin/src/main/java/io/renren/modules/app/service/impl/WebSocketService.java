//package io.renren.modules.app.service.impl;
//
//import io.renren.common.constant.Constant;
//import io.renren.modules.app.common.Utils;
//import io.renren.modules.app.message.Context;
//import io.renren.modules.app.message.MessageHandler;
//import io.renren.modules.app.message.proto.Message;
//import io.vertx.core.Vertx;
//import io.vertx.core.http.ClientAuth;
//import io.vertx.core.http.HttpServerOptions;
//import io.vertx.core.http.ServerWebSocket;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections.CollectionUtils;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.ApplicationContext;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.List;
//import java.util.concurrent.ConcurrentHashMap;
//
//import static io.vertx.core.http.HttpVersion.*;
//
//@Service
//@Slf4j
//public class WebSocketService implements InitializingBean {
//
//    @Value("${app.ws.port}")
//    private Integer port;
//    @Resource
//    private Vertx vertx;
//    private final ConcurrentHashMap<String, ServerWebSocket> deviceMap = new ConcurrentHashMap<>();
//    private final ConcurrentHashMap<String, ServerWebSocket> monitorMap = new ConcurrentHashMap<>();
//
//
//    private ConcurrentHashMap<String, List<MessageHandler>> handlerMap = new ConcurrentHashMap<>();
//
//    @Resource
//    private ApplicationContext applicationContext;
//
//    private String handlerKey(int source, int type) {
//        return String.format("%s:%s", source, type);
//    }
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        HttpServerOptions options = new HttpServerOptions();
//        options.setSsl(false);
//        options.setClientAuth(ClientAuth.NONE);
//        options.setHost("0.0.0.0");
//        options.setPort(8082);
//        options.setLogActivity(true);
//        options.setAlpnVersions(Arrays.asList(HTTP_1_1, HTTP_1_0, HTTP_2));
//        vertx.createHttpServer(options).webSocketHandler(serverWebSocket -> {
//
//                    log.info("connect");
//                    serverWebSocket.handler(buffer -> {
//                        //TODO 使用线程池
//                        byte[] bytes = buffer.getBytes();
//
//                        try {
//                            //解压
////                    long size = Zstd.getFrameContentSize(buffer.getBytes());
////                    byte[] _bytes = Zstd.decompress(bytes, (int) size);
//
//                            //序列化
//                            Message.WsMessage wsMessage = Message.WsMessage.parseFrom(bytes);
//                            //decode
//                            //获取身份
//                            int source = wsMessage.getSource();
//                            int type = wsMessage.getType();
//                            String key = handlerKey(source, type);
//                            List<MessageHandler> messageHandlers = handlerMap.get(key);
//                            if (CollectionUtils.isEmpty(messageHandlers)) {
//                                log.warn("未设置消息处理器:{}", key);
//                            } else {
//                                byte[] body = wsMessage.getBody().toByteArray();
//                                //解压?
//                                byte[] _body = Utils.decompress(body);
//                                Object _message = null;
//                                //反序列化
//                                switch (type) {
//                                    case Constant.MessageType.android_online:
//                                        _message = Message.AndroidOnline.parseFrom(_body);
//                                        break;
//                                    case Constant.MessageType.screen_info:
//                                        _message = Message.ScreenInfo.parseFrom(_body);
//                                        break;
//                                    case Constant.MessageType.touch_req:
//                                        _message = Message.TouchReq.parseFrom(_body);
//                                        break;
//                                    case Constant.MessageType.scroll_req:
//                                        _message = Message.ScrollReq.parseFrom(_body);
//                                        break;
//                                    case Constant.MessageType.back_req:
//                                        _message = Message.BackReq.parseFrom(_body);
//                                        break;
//                                    case Constant.MessageType.home_req:
//                                        _message = Message.HomeReq.parseFrom(_body);
//                                        break;
//                                    case Constant.MessageType.monitor_online:
//                                        _message = Message.MonitorOnline.parseFrom(_body);
//                                        break;
//                                    default:
//                                        throw new RuntimeException("消息转换错误!");
//
//                                }
//
//
//                                Context context = new Context(deviceMap, monitorMap, serverWebSocket);
//                                for (MessageHandler handlerHandler : messageHandlers) {
//                                    try {
//                                        handlerHandler.handler(_message, context);
//                                    } catch (Exception ex) {
//                                        log.warn("handler error", ex);
//                                    }
//                                }
//                            }
//
//
//                        } catch (Exception e) {
//                            log.warn("处理错误:", e);
//                        }
//
//
//                    });
//                    //TODO 3秒 踢下线?
//                    //TODO 循环检擦状态
//                    serverWebSocket.closeHandler(v -> {
//                        //关闭
//                        log.info("关闭!");
//                    });
//
//                }).listen(8082)
//                .onSuccess(s -> {
//
//                    vertx.createHttpClient().webSocket(8082, "127.0.0.1", "/", result -> {
//                        if (result.succeeded()) {
//                            log.info("连接成功");
//                        } else {
//                            log.error("连接失败", result.cause());
//                        }
//                    });
//                    log.info("ws开启成功:{}", port);
//                }).onFailure(ex -> {
//                    log.error("ws开启失败", ex);
//                });
//
//
//        Collection<MessageHandler> values = applicationContext.getBeansOfType(MessageHandler.class).values();
//
//        for (MessageHandler messageHandler : values) {
//            int type = messageHandler.type();
//            int source = messageHandler.source();
//
//            String key = handlerKey(source, type);
//            List<MessageHandler> messageHandlers = handlerMap.computeIfAbsent(key, k -> new ArrayList<>());
//            messageHandlers.add(messageHandler);
//        }
//    }
//}
