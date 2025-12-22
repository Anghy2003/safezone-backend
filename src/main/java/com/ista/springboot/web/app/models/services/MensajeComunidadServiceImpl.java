package com.ista.springboot.web.app.models.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.ista.springboot.web.app.models.dao.IMensajeComunidad;
import com.ista.springboot.web.app.models.entity.MensajeComunidad;

@Service
public class MensajeComunidadServiceImpl implements IMensajeComunidadService {

    @Autowired
    private IMensajeComunidad mensajeDao;

    @Override
    public List<MensajeComunidad> findAll() {
        return mensajeDao.findAll();
    }

    @Override
    public List<MensajeComunidad> findByComunidad(Long comunidadId) {
        return mensajeDao.findByComunidad_IdOrderByFechaEnvioAsc(comunidadId);
    }

    @Override
    public List<MensajeComunidad> findByComunidadAndCanal(Long comunidadId, String canal) {
        return mensajeDao.findByComunidad_IdAndCanalOrderByFechaEnvioAsc(comunidadId, canal);
    }

    @Override
    public Page<MensajeComunidad> findPageByComunidadAndCanal(Long comunidadId, String canal, int page, int size) {
        return mensajeDao.findByComunidad_IdAndCanalOrderByFechaEnvioDesc(comunidadId, canal, PageRequest.of(page, size));
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
