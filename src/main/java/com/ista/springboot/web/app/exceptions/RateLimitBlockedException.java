package com.ista.springboot.web.app.exceptions;

import java.time.OffsetDateTime;

public class RateLimitBlockedException extends RuntimeException {
    private final OffsetDateTime blockedUntil;

    public RateLimitBlockedException(String message, OffsetDateTime blockedUntil) {
        super(message);
        this.blockedUntil = blockedUntil;
    }

    public OffsetDateTime getBlockedUntil() {
        return blockedUntil;
    }
}
