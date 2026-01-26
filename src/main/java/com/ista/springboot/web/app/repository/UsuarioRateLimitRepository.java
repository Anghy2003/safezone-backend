package com.ista.springboot.web.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.ista.springboot.web.app.models.entity.UsuarioRateLimit;

import jakarta.persistence.LockModeType;

public interface UsuarioRateLimitRepository extends JpaRepository<UsuarioRateLimit, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM UsuarioRateLimit r WHERE r.usuarioId = :usuarioId")
    Optional<UsuarioRateLimit> findByUsuarioIdForUpdate(@Param("usuarioId") Long usuarioId);
}
