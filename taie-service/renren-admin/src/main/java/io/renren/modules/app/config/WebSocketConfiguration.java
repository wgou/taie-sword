package io.renren.modules.app.config;

import io.renren.modules.app.web.ws.WebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

@Configuration
public class WebSocketConfiguration implements WebSocketConfigurer {

    @Resource
    private WebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
//                .addHandler(monitorWebSocketHandler, "monitor")
                .addHandler(webSocketHandler, "ws")
                .setAllowedOrigins("*");  // 允许所有来源，生产环境中请根据需要调整
    }
}
