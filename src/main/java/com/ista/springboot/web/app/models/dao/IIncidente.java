package com.ista.springboot.web.app.models.dao;

import org.springframework.data.repository.CrudRepository;
import com.ista.springboot.web.app.models.entity.Incidente;

public interface IIncidente extends CrudRepository<Incidente, Long> {

}
