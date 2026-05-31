package com.neocaps.api.config;

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
        // Prefix for outgoing messages from server to clients
        config.enableSimpleBroker("/topic");
        // Prefix for incoming messages from clients to handlers annotated with @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register websocket endpoint for PLC or HMI connection
        registry.addEndpoint("/ws-plc")
                .setAllowedOriginPatterns("*");
        
        // SockJS fallback option for web browser clients
        registry.addEndpoint("/ws-plc")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
