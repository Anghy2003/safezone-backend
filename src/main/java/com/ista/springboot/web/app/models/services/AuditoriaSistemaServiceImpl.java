package com.ista.springboot.web.app.models.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ista.springboot.web.app.models.dao.IAuditoriaSistema;
import com.ista.springboot.web.app.models.entity.AuditoriaSistema;

@Service
public class AuditoriaSistemaServiceImpl implements IAuditoriaSistemaService {

    @Autowired
    private IAuditoriaSistema auditoriaDao;

    @Override
    public List<AuditoriaSistema> findAll() {
        return (List<AuditoriaSistema>) auditoriaDao.findAll();
    }

    @Override
    public AuditoriaSistema save(AuditoriaSistema auditoria) {
        return auditoriaDao.save(auditoria);
    }

    @Override
    public AuditoriaSistema findById(Long id) {
        return auditoriaDao.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        auditoriaDao.deleteById(id);
    }
}
