// src/main/java/com/ista/springboot/web/app/config/UserHandshakeHandler.java
package com.ista.springboot.web.app.config;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.lang.Nullable;
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

        if (userId == null || userId.isEmpty()) {
            // si no hay userId, caemos al comportamiento default (pero NO servirá para /user/queue)
            return super.determineUser(request, wsHandler, attributes);
        }

        return new StompPrincipal(userId);
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
