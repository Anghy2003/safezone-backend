package com.ista.springboot.web.app.models.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ista.springboot.web.app.models.entity.UsuarioComunidad;

public interface IUsuarioComunidad extends JpaRepository<UsuarioComunidad, Long> {

    long countByComunidadId(Long comunidadId);

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndComunidadId(Long usuarioId, Long comunidadId);

    boolean existsByUsuarioIdAndComunidadIdAndEstadoIgnoreCase(Long usuarioId, Long comunidadId, String estado);

    Optional<UsuarioComunidad> findByUsuarioIdAndComunidadId(Long usuarioId, Long comunidadId);

    Optional<UsuarioComunidad> findFirstByUsuarioId(Long usuarioId);

    List<UsuarioComunidad> findByUsuarioId(Long usuarioId);

    // === Roles ===
    boolean existsByRolIgnoreCase(String rol);
    Optional<UsuarioComunidad> findFirstByRolIgnoreCase(String rol);
    boolean existsByUsuarioIdAndRolIgnoreCase(Long usuarioId, String rol);

    boolean existsByUsuarioIdAndComunidadIdAndRolIgnoreCaseAndEstadoIgnoreCase(
            Long usuarioId, Long comunidadId, String rol, String estado
    );

    // === Solicitudes pendientes y activos por comunidad ===
    @EntityGraph(attributePaths = {"usuario", "comunidad"})
    List<UsuarioComunidad> findByComunidadIdAndEstadoIgnoreCase(Long comunidadId, String estado);

    // === Mis comunidades activas ===
    @EntityGraph(attributePaths = {"comunidad"})
    List<UsuarioComunidad> findByUsuarioIdAndEstadoIgnoreCase(Long usuarioId, String estado);
}
