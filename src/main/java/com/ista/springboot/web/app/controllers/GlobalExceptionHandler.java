package com.ista.springboot.web.app.controllers;

import java.util.Map;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.ista.springboot.web.app.exceptions.RateLimitBlockedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitBlockedException.class)
    public ResponseEntity<?> handleRateLimit(RateLimitBlockedException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                Map.of(
                        "message", e.getMessage(),
                        "blockedUntil", e.getBlockedUntil() != null ? e.getBlockedUntil().toString() : null
                )
        );
    }
}
