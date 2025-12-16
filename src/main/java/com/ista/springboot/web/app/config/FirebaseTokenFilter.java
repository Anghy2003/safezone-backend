package com.ista.springboot.web.app.config;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

@Component
public class FirebaseTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Si no hay token, seguimos normal (Spring Security decidirá si el endpoint requiere auth)
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();
        if (token.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        try {
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(token);

            String name = (String) decoded.getClaims().get("name");
            String picture = (String) decoded.getClaims().get("picture");

            FirebasePrincipal principal =
                    new FirebasePrincipal(decoded.getUid(), decoded.getEmail(), name, picture);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, List.of());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);

        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Token Firebase inválido o expirado\"}");
        }
    }
}
