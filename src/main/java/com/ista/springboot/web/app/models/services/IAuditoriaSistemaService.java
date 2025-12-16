package com.ista.springboot.web.app.models.services;

import java.util.List;
import com.ista.springboot.web.app.models.entity.AuditoriaSistema;

public interface IAuditoriaSistemaService {
    public List<AuditoriaSistema> findAll();
    public AuditoriaSistema save(AuditoriaSistema auditoria);
    public AuditoriaSistema findById(Long id);
    public void delete(Long id);
}
