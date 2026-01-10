package com.ista.springboot.web.app.models.dao;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.ista.springboot.web.app.models.entity.ComunidadInvitacion;

public interface IComunidadInvitacion extends CrudRepository<ComunidadInvitacion, Long> {
  Optional<ComunidadInvitacion> findByToken(String token);
}
