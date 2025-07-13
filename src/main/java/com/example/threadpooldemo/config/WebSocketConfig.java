package com.example.threadpooldemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * @author zh4ngyj
 * @date 2025/7/13
 * @description
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ThreadPoolWebSocketHandler webSocketHandler;

    @Autowired
    public WebSocketConfig(ThreadPoolWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
        System.out.println("WebSocket handler 注入成功: " + webSocketHandler);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws/thread-pool")
                // 允许跨域，生产环境应限制域名
                .setAllowedOrigins("*");
    }
}
