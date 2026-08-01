package com.tesla.teslabackend.group.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Broker STOMP en memoria para el chat de grupos.
 *
 * <p>Antes se usaba un relay hacia Amazon MQ (RabbitMQ con STOMP), eliminado en la
 * optimizacion FinOps. El broker simple no comparte estado entre procesos, por lo
 * que <strong>solo es correcto mientras el servicio ECS corra una unica tarea</strong>
 * ({@code desired_count = 1}, sin autoscaling). Si se vuelve a escalar en horizontal
 * hara falta un broker externo de nuevo.</p>
 *
 * <p>Ademas, el chat no esta expuesto publicamente: API Gateway HTTP API no hace
 * proxy de upgrades WebSocket, asi que el behavior /ws-chat/* se retiro de
 * CloudFront. El endpoint sigue operativo en local (docker compose) y accesible
 * directamente contra la tarea.</p>
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class GroupWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/queue");
    }
}
