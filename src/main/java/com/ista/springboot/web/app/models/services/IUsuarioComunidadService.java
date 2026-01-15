package com.ista.springboot.web.app.models.services;

import java.util.List;
import java.util.Map;

import com.ista.springboot.web.app.models.entity.UsuarioComunidad;

public interface IUsuarioComunidadService {

    // Legacy
    UsuarioComunidad unirUsuarioAComunidad(Long usuarioId, String codigoAcceso);

    UsuarioComunidad unirUsuarioPorToken(Long usuarioId, String token);

    // Nuevo: entra directo si conoce el código
    UsuarioComunidad unirsePorCodigo(Long usuarioId, String codigoAcceso);

    void requireAdminComunidad(Long adminId, Long comunidadId);

    // Flujo solicitud
    UsuarioComunidad solicitarUnirse(Long usuarioId, Long comunidadId);

    List<UsuarioComunidad> listarSolicitudesPendientes(Long adminId, Long comunidadId);

    UsuarioComunidad aprobarSolicitud(Long adminId, Long comunidadId, Long usuarioId);

    void rechazarSolicitud(Long adminId, Long comunidadId, Long usuarioId);

    // Compat legacy token
    Map<String, Object> aprobarSolicitudYGenerarToken(Long adminId, Long comunidadId, Long usuarioId, Integer horasExpira);
}
