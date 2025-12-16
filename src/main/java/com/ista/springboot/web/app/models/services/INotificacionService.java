package com.ista.springboot.web.app.models.services;

import java.util.List;
import com.ista.springboot.web.app.models.entity.Notificacion;

public interface INotificacionService {
    public List<Notificacion> findAll();
    public Notificacion save(Notificacion notificacion);
    public Notificacion findById(Long id);
    public void delete(Long id);
}
