package com.ista.springboot.web.app.models.dao;

import org.springframework.data.repository.CrudRepository;
import com.ista.springboot.web.app.models.entity.Notificacion;

public interface INotificacion extends CrudRepository<Notificacion, Long> {

}
