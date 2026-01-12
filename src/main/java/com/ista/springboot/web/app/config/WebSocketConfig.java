package com.ista.springboot.web.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración WebSocket con STOMP
 *
 * Permite:
 * - Comunicación en tiempo real (chat)
 * - Topics por comunidad
 * - Compatibilidad SockJS
 *
 * Sugerencias de topics (no obligatorias):
 * - /topic/comunidad.{comunidadId}.chat
 * - /topic/comunidad.{comunidadId}.solicitudes   (admins)
 * - /user/queue/*                                (privados)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        config.enableSimpleBroker(
                "/topic",
                "/queue"
        );

        config.setApplicationDestinationPrefixes("/app");

        // Opcional (si usas mensajes privados con /user/queue)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
