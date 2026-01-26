package com.ista.springboot.web.app.models.services;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class ResetTokenService {

    @Value("${app.reset.jwt-secret}")
    private String jwtSecret;

    private static final int TTL_MIN = 30;

    public String createResetToken(String email) {
        OffsetDateTime now = OffsetDateTime.now();
        Date iat = Date.from(now.toInstant());
        Date exp = Date.from(now.plusMinutes(TTL_MIN).toInstant());

        return Jwts.builder()
                .setSubject(email.trim().toLowerCase())
                .setIssuedAt(iat)
                .setExpiration(exp)
                .claim("typ", "pwd_reset")
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public String validateAndGetEmail(String token) {
        Claims c = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        if (!"pwd_reset".equals(String.valueOf(c.get("typ")))) {
            throw new IllegalArgumentException("Token inválido");
        }
        String email = c.getSubject();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Token inválido");
        }
        return email.trim().toLowerCase();
    }
}
