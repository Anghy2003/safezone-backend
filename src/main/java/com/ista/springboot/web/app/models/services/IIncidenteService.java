package com.ista.springboot.web.app.models.services;

import java.util.List;
import com.ista.springboot.web.app.models.entity.Incidente;

public interface IIncidenteService {
    public List<Incidente> findAll();
    public Incidente save(Incidente incidente);
    public Incidente findById(Long id);
    public void delete(Long id);
}
