package com.ista.springboot.web.app.models.dao;

import org.springframework.data.repository.CrudRepository;
import com.ista.springboot.web.app.models.entity.AuditoriaSistema;

public interface IAuditoriaSistema extends CrudRepository<AuditoriaSistema, Long> {

}
