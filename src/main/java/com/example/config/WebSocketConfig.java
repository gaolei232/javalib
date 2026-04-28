package com.example.config;

import com.example.websocket.SeatStatusWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SeatStatusWebSocketHandler seatStatusWebSocketHandler;

    @Autowired
    public WebSocketConfig(SeatStatusWebSocketHandler seatStatusWebSocketHandler) {
        this.seatStatusWebSocketHandler = seatStatusWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(seatStatusWebSocketHandler, "/ws/seat-status")
                .setAllowedOrigins("*");
    }
}