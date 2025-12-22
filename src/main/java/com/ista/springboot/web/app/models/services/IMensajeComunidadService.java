package com.ista.springboot.web.app.models.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.ista.springboot.web.app.models.entity.MensajeComunidad;

public interface IMensajeComunidadService {

    List<MensajeComunidad> findAll();

    // ✅ Historial
    List<MensajeComunidad> findByComunidad(Long comunidadId);
    List<MensajeComunidad> findByComunidadAndCanal(Long comunidadId, String canal);

    // ✅ Opcional paginado (últimos primero)
    Page<MensajeComunidad> findPageByComunidadAndCanal(Long comunidadId, String canal, int page, int size);

    MensajeComunidad save(MensajeComunidad mensaje);

    MensajeComunidad findById(Long id);

    void delete(Long id);
}
