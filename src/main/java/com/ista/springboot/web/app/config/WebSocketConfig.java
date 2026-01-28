// src/main/java/com/ista/springboot/web/app/config/WebSocketConfig.java
package com.ista.springboot.web.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * ✅ WebSocket con STOMP:
 * - Broker: /topic y /queue
 * - App prefix: /app
 * - User prefix: /user
 *
 * ✅ Importante para "usuarios cercanos":
 * Usaremos /user/queue/* para enviar mensajes a usuarios específicos (por userId).
 * Para eso necesitamos Principal => lo resolvemos con UserIdHandshakeInterceptor + UserHandshakeHandler.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        config.enableSimpleBroker("/topic", "/queue");

        config.setApplicationDestinationPrefixes("/app");

        // ✅ Esto habilita /user/queue/*
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                // ✅ 1) lee userId del querystring y lo guarda
                .addInterceptors(new UserIdHandshakeInterceptor())
                // ✅ 2) crea el Principal (name = userId)
                .setHandshakeHandler(new UserHandshakeHandler())
                .withSockJS();
    }
}