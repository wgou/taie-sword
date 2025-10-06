package io.renren.modules.app.config;

import javax.annotation.Resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.context.annotation.Bean;

import io.renren.modules.app.web.ws.WebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    @Resource
    private WebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(webSocketHandler, "ws")
                .setAllowedOrigins("*");  // 允许所有来源，生产环境中请根据需要调整
//                .withSockJS()
//                .setHttpMessageCacheSize(1024 * 1024);  // 设置 HTTP 消息缓存大小为 1MB
    }

    /**
     * 配置 WebSocket 容器，设置消息缓存大小为 1MB
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(1024 * 1024);  // 设置文本消息缓存大小为 1MB
        container.setMaxBinaryMessageBufferSize(1024 * 1024); // 设置二进制消息缓存大小为 1MB
        container.setMaxSessionIdleTimeout(300000L); // 设置会话空闲超时时间为 5 分钟
        return container;
    }
}
