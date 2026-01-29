package com.ista.springboot.web.app.config;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

public class UserHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(
            ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        Object raw = attributes.get(UserIdHandshakeInterceptor.ATTR_USER_ID);
        final String userId = (raw != null) ? raw.toString().trim() : null;

        // ✅ Si NO hay userId, igual devuelve un Principal válido (SockJS estable)
        final String principalName = (userId != null && !userId.isEmpty())
                ? userId
                : UUID.randomUUID().toString();

        return new StompPrincipal(principalName);
    }

    private static final class StompPrincipal implements Principal {
        private final String name;

        private StompPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
