package com.pulsewire.dataplane.gateway;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration for the data plane gateway.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketGateway webSocketGateway;

    public WebSocketConfig(WebSocketGateway webSocketGateway) {
        this.webSocketGateway = webSocketGateway;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketGateway, "/ws/market-data")
                .setAllowedOrigins("*");
    }
}
