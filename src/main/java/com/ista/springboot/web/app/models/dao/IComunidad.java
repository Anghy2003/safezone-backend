package com.ista.springboot.web.app.models.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.ista.springboot.web.app.models.entity.Comunidad;
import com.ista.springboot.web.app.models.entity.EstadoComunidad;

public interface IComunidad extends CrudRepository<Comunidad, Long> {

    Comunidad findByCodigoAcceso(String codigoAcceso);

    // 🔹 Para listar solicitudes en el panel del admin
    List<Comunidad> findByEstado(EstadoComunidad estado);
}
