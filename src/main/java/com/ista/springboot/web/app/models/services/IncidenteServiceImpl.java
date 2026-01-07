package com.ista.springboot.web.app.models.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ista.springboot.web.app.models.dao.IIncidente;
import com.ista.springboot.web.app.models.entity.Incidente;

@Service
public class IncidenteServiceImpl implements IIncidenteService {

    @Autowired
    private IIncidente incidenteDao;

    @Override
    public List<Incidente> findAll() {
        return (List<Incidente>) incidenteDao.findAll();
    }

    @Override
    public Incidente save(Incidente incidente) {
        return incidenteDao.save(incidente);
    }

    @Override
    public Incidente findById(Long id) {
        return incidenteDao.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        incidenteDao.deleteById(id);
    }

    @Override
    public Incidente findByClientGeneratedId(String clientGeneratedId) {
        if (clientGeneratedId == null || clientGeneratedId.trim().isEmpty()) return null;
        return incidenteDao.findFirstByClientGeneratedId(clientGeneratedId.trim());
    }
}
