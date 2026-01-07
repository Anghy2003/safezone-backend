package com.ista.springboot.web.app.models.services;

import java.util.List;
import com.ista.springboot.web.app.models.entity.Incidente;

public interface IIncidenteService {
    List<Incidente> findAll();
    Incidente save(Incidente incidente);
    Incidente findById(Long id);
    void delete(Long id);

    // ✅ CASO A: idempotencia
    Incidente findByClientGeneratedId(String clientGeneratedId);
}
