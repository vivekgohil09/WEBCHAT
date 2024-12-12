package com.chat.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enabling a simple message broker
        config.enableSimpleBroker("/topic", "/queue");  // Subscribe to these destinations
        config.setApplicationDestinationPrefixes("/app"); // Prefix for sending messages
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Enable the WebSocket endpoint for client to connect
        registry.addEndpoint("/chat").withSockJS();  // Use SockJS for fallback options
    }
}
