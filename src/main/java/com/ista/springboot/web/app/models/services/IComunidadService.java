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

    // 🔹 Usuario solicita crear comunidad (nombre, dirección, ubicación)
    Comunidad solicitarComunidad(Comunidad comunidad);

    // 🔹 Admin aprueba una comunidad solicitada y genera el código
    Comunidad aprobarComunidad(Long comunidadId);
}
