package com.ista.springboot.web.app.models.services;

import java.util.List;
import java.util.Map;

import com.ista.springboot.web.app.models.entity.UsuarioComunidad;

public interface IUsuarioComunidadService {

    UsuarioComunidad unirUsuarioAComunidad(Long usuarioId, String codigoAcceso);

    UsuarioComunidad unirUsuarioPorToken(Long usuarioId, String token);

    void requireAdminComunidad(Long adminId, Long comunidadId);

    // ===== NUEVO FLUJO SOLICITUD =====
    UsuarioComunidad solicitarUnirse(Long usuarioId, Long comunidadId);

    List<UsuarioComunidad> listarSolicitudesPendientes(Long adminId, Long comunidadId);

    Map<String, Object> aprobarSolicitudYGenerarToken(Long adminId, Long comunidadId, Long usuarioId, Integer horasExpira);

    void rechazarSolicitud(Long adminId, Long comunidadId, Long usuarioId);
    UsuarioComunidad aprobarSolicitud(Long adminId, Long comunidadId, Long usuarioId);
   


}
