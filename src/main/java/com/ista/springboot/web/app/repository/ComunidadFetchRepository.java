package com.ista.springboot.web.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.ista.springboot.web.app.models.entity.Comunidad;

public interface ComunidadFetchRepository extends JpaRepository<Comunidad, Long> {

    @Query("""
        SELECT DISTINCT c
        FROM Comunidad c
        LEFT JOIN FETCH c.usuarioComunidades uc
        LEFT JOIN FETCH uc.usuario u
        WHERE c.id = :id
          AND (uc IS NULL OR uc.estado = 'activo')
    """)
    Optional<Comunidad> findByIdWithMiembrosActivos(@Param("id") Long id);
}