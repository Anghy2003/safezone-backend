package com.ista.springboot.web.app.models.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario_rate_limit")
public class UsuarioRateLimit implements Serializable {

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "window_start", nullable = false)
    private OffsetDateTime windowStart;

    @Column(name = "count", nullable = false)
    private Integer count;

    @Column(name = "blocked_until")
    private OffsetDateTime blockedUntil;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (windowStart == null) windowStart = now;
        if (count == null) count = 0;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // Getters/Setters
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public OffsetDateTime getWindowStart() { return windowStart; }
    public void setWindowStart(OffsetDateTime windowStart) { this.windowStart = windowStart; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

    public OffsetDateTime getBlockedUntil() { return blockedUntil; }
    public void setBlockedUntil(OffsetDateTime blockedUntil) { this.blockedUntil = blockedUntil; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    private static final long serialVersionUID = 1L;
}
