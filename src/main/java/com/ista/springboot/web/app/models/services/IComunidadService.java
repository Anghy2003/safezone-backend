package com.ista.springboot.web.app.models.services;

import java.util.List;

import com.ista.springboot.web.app.models.entity.Comunidad;
import com.ista.springboot.web.app.models.entity.EstadoComunidad;

public interface IComunidadService {

    List<Comunidad> findAll();

    Comunidad save(Comunidad comunidad);

    Comunidad findById(Long id);

    void delete(Long id);

    Comunidad findByCodigoAcceso(String codigoAcceso);

    List<Comunidad> findByEstado(EstadoComunidad estado);

    // ✅ recibe usuarioId para enlazar solicitante (SMS)
    Comunidad solicitarComunidad(Comunidad comunidad, Long usuarioId);

    // ✅ al aprobar: genera código y envía SMS
    Comunidad aprobarComunidad(Long comunidadId);

    Comunidad findByIdWithMiembrosActivos(Long id);
}
