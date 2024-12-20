package com.example.thiscode.config;

import com.example.thiscode.controller.CustomWebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private final CustomWebSocketController customWebSocketController;

    public WebSocketConfig(CustomWebSocketController customWebSocketController) {
        this.customWebSocketController = customWebSocketController;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(customWebSocketController, "/ws").setAllowedOrigins("*");
    }
}
