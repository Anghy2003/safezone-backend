package com.ista.springboot.web.app.models.dao;

import org.springframework.data.repository.CrudRepository;
import com.ista.springboot.web.app.models.entity.ContactoEmergencia;

public interface IContactoEmergencia extends CrudRepository<ContactoEmergencia, Long> {

}
