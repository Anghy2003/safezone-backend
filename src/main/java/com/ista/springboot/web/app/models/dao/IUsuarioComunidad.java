package com.ista.springboot.web.app.models.dao;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ista.springboot.web.app.models.entity.UsuarioComunidad;

public interface IUsuarioComunidad extends JpaRepository<UsuarioComunidad, Long> {

    // Contar miembros por comunidad
    long countByComunidadId(Long comunidadId);

    // Regla: usuario solo puede estar en UNA comunidad
    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndComunidadId(Long usuarioId, Long comunidadId);

    Optional<UsuarioComunidad> findByUsuarioIdAndComunidadId(Long usuarioId, Long comunidadId);

    Optional<UsuarioComunidad> findFirstByUsuarioId(Long usuarioId);

    List<UsuarioComunidad> findByUsuarioId(Long usuarioId);

    // Admin único global (en toda la tabla)
    boolean existsByRolIgnoreCase(String rol);

    Optional<UsuarioComunidad> findFirstByRolIgnoreCase(String rol);

    // Verificar si ESTE usuario es ADMIN
    boolean existsByUsuarioIdAndRolIgnoreCase(Long usuarioId, String rol);
}
