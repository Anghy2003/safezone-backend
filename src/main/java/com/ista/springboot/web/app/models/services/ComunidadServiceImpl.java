package com.ista.springboot.web.app.models.services;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ista.springboot.web.app.models.dao.IComunidad;
import com.ista.springboot.web.app.models.dao.IUsuarioComunidad;
import com.ista.springboot.web.app.models.entity.Comunidad;
import com.ista.springboot.web.app.models.entity.EstadoComunidad;

@Service
public class ComunidadServiceImpl implements IComunidadService {

    @Autowired
    private IComunidad comunidadDao;

    @Autowired
    private IUsuarioComunidad usuarioComunidadDao;   // 👈 NECESARIO PARA CONTAR MIEMBROS

    @Override
    public List<Comunidad> findAll() {
        List<Comunidad> comunidades = (List<Comunidad>) comunidadDao.findAll();

        // 🔹 Añadimos cantidad de miembros
        for (Comunidad c : comunidades) {
            long count = usuarioComunidadDao.countByComunidadId(c.getId());
            c.setMiembrosCount(count);  // 👈 Campo @Transient en la entidad
        }

        return comunidades;
    }

    @Override
    public Comunidad findById(Long id) {
        Comunidad comunidad = comunidadDao.findById(id).orElse(null);

        if (comunidad != null) {
            long count = usuarioComunidadDao.countByComunidadId(id);
            comunidad.setMiembrosCount(count);
        }

        return comunidad;
    }

    @Override
    public Comunidad save(Comunidad comunidad) {
        return comunidadDao.save(comunidad);
    }

    @Override
    public void delete(Long id) {
        comunidadDao.deleteById(id);
    }

    @Override
    public Comunidad findByCodigoAcceso(String codigoAcceso) {
        Comunidad comunidad = comunidadDao.findByCodigoAcceso(codigoAcceso);

        if (comunidad != null) {
            long count = usuarioComunidadDao.countByComunidadId(comunidad.getId());
            comunidad.setMiembrosCount(count);
        }

        return comunidad;
    }

    @Override
    public List<Comunidad> findByEstado(EstadoComunidad estado) {
        List<Comunidad> comunidades = comunidadDao.findByEstado(estado);

        for (Comunidad c : comunidades) {
            long count = usuarioComunidadDao.countByComunidadId(c.getId());
            c.setMiembrosCount(count);
        }

        return comunidades;
    }

    // 🔹 Cuando el usuario solicita comunidad desde la app
    @Override
    public Comunidad solicitarComunidad(Comunidad comunidad) {
        comunidad.setId(null);
        comunidad.setEstado(EstadoComunidad.SOLICITADA);
        comunidad.setActiva(false);
        comunidad.setCodigoAcceso(null);
        return comunidadDao.save(comunidad);
    }

    // 🔹 Cuando el admin la aprueba y genera código
    @Override
    public Comunidad aprobarComunidad(Long comunidadId) {
        Comunidad comunidad = comunidadDao.findById(comunidadId)
            .orElseThrow(() -> new RuntimeException("Comunidad no encontrada"));

        if (comunidad.getEstado() != EstadoComunidad.SOLICITADA) {
            throw new RuntimeException("La comunidad no está en estado SOLICITADA");
        }

        comunidad.setEstado(EstadoComunidad.ACTIVA);
        comunidad.setActiva(true);
        comunidad.setCodigoAcceso(generarCodigo5());

        return comunidadDao.save(comunidad);
    }

    // 🔸 Generate random 5-digit code
    private String generarCodigo5() {
        Random r = new Random();
        int numero = 10000 + r.nextInt(90000);
        return String.valueOf(numero);
    }
}
