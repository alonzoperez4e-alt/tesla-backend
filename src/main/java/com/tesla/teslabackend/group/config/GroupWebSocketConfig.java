package com.tesla.teslabackend.group.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class GroupWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.rabbitmq.host}")
    private String relayHost;

    @Value("${spring.rabbitmq.port}")
    private String relayPort;

    @Value("${spring.rabbitmq.username}")
    private String clientLogin;

    @Value("${spring.rabbitmq.password}")
    private String clientPasscode;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");

        registry.enableStompBrokerRelay("/topic")
                .setRelayHost(relayHost)
                .setRelayPort(Integer.parseInt(relayPort))
                .setClientLogin(clientLogin)
                .setClientPasscode(clientPasscode)
                .setSystemLogin(clientLogin)
                .setSystemPasscode(clientPasscode);
    }
}