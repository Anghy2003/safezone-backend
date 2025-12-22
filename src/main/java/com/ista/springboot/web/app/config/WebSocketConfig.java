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
 * - Uso de topics por comunidad y vecinos
 * - Compatibilidad con SockJS (Flutter / Web / Angular)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configura el broker de mensajes
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        // 👉 Broker simple en memoria (suficiente para tu caso)
        // Todo lo que empiece con /topic o /queue será enviado a los clientes suscritos
        config.enableSimpleBroker(
                "/topic",   // mensajes públicos (comunidad, vecinos)
                "/queue"    // mensajes privados (si luego los usas)
        );

        // 👉 Prefijo para mensajes que van DESDE el cliente AL backend
        // Flutter enviará a /app/chat/comunidad o /app/chat/vecinos
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registra el endpoint WebSocket
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // 👉 Endpoint principal de conexión WebSocket
        // Flutter / Web se conectan a: http://HOST:PUERTO/ws
        registry.addEndpoint("/ws")
                // Permite conexiones desde cualquier origen (OK para desarrollo)
                .setAllowedOriginPatterns("*")
                // Habilita SockJS para compatibilidad (fallback si WS puro falla)
                .withSockJS();
    }
}
