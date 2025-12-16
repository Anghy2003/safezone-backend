package com.ista.springboot.web.app.models.services;

import java.util.List;
import com.ista.springboot.web.app.models.entity.MensajeComunidad;

public interface IMensajeComunidadService {
    public List<MensajeComunidad> findAll();
    public MensajeComunidad save(MensajeComunidad mensaje);
    public MensajeComunidad findById(Long id);
    public void delete(Long id);
}
