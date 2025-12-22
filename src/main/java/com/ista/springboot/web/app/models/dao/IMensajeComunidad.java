package com.ista.springboot.web.app.models.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ista.springboot.web.app.models.entity.MensajeComunidad;

public interface IMensajeComunidad extends JpaRepository<MensajeComunidad, Long> {

    List<MensajeComunidad> findByComunidad_IdOrderByFechaEnvioAsc(Long comunidadId);

    List<MensajeComunidad> findByComunidad_IdAndCanalOrderByFechaEnvioAsc(Long comunidadId, String canal);

    // Paginado (últimos primero)
    Page<MensajeComunidad> findByComunidad_IdAndCanalOrderByFechaEnvioDesc(
            Long comunidadId, String canal, Pageable pageable
    );
}
