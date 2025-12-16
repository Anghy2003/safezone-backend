package com.ista.springboot.web.app.models.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ista.springboot.web.app.models.dao.IMensajeComunidad;
import com.ista.springboot.web.app.models.entity.MensajeComunidad;

@Service
public class MensajeComunidadServiceImpl implements IMensajeComunidadService {

    @Autowired
    private IMensajeComunidad mensajeDao;

    @Override
    public List<MensajeComunidad> findAll() {
        return (List<MensajeComunidad>) mensajeDao.findAll();
    }

    @Override
    public MensajeComunidad save(MensajeComunidad mensaje) {
        return mensajeDao.save(mensaje);
    }

    @Override
    public MensajeComunidad findById(Long id) {
        return mensajeDao.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        mensajeDao.deleteById(id);
    }
}
