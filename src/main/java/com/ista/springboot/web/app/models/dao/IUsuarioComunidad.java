package com.ista.springboot.web.app.models.dao;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ista.springboot.web.app.models.entity.UsuarioComunidad;

public interface IUsuarioComunidad extends JpaRepository<UsuarioComunidad, Long> {

    // 👉 Para contar miembros por comunidad
    long countByComunidadId(Long comunidadId);

    // 👉 Para validar que un usuario solo esté en UNA comunidad
    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndComunidadId(Long usuarioId, Long comunidadId);

    Optional<UsuarioComunidad> findByUsuarioIdAndComunidadId(Long usuarioId, Long comunidadId);

    Optional<UsuarioComunidad> findFirstByUsuarioId(Long usuarioId);

    // (opcional, si un día quieres listar todas las comunidades de un usuario)
    List<UsuarioComunidad> findByUsuarioId(Long usuarioId);
}
