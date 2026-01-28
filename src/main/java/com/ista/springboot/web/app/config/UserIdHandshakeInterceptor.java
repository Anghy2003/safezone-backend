// src/main/java/com/ista/springboot/web/app/config/UserIdHandshakeInterceptor.java
package com.ista.springboot.web.app.config;

import java.net.URI;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

public class UserIdHandshakeInterceptor implements HandshakeInterceptor {

    public static final String ATTR_USER_ID = "WS_USER_ID";

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String userId = null;

        // 1) querystring ?userId=123
        try {
            URI uri = request.getURI();
            MultiValueMap<String, String> params =
                    UriComponentsBuilder.fromUri(uri).build().getQueryParams();
            userId = params.getFirst("userId");
        } catch (Exception ignored) {}

        // 2) fallback header (opcional) "X-User-Id: 123"
        if (userId == null || userId.trim().isEmpty()) {
            userId = request.getHeaders().getFirst("X-User-Id");
        }

        if (userId == null || userId.trim().isEmpty()) {
            // ❗ Sin userId no hay /user/queue routing
            return false;
        }

        attributes.put(ATTR_USER_ID, userId.trim());
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            @Nullable Exception exception
    ) {
        // no-op
    }
}
