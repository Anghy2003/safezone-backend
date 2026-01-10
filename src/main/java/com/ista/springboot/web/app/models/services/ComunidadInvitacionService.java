package com.ista.springboot.web.app.models.services;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ista.springboot.web.app.models.dao.IComunidadInvitacion;
import com.ista.springboot.web.app.models.entity.Comunidad;
import com.ista.springboot.web.app.models.entity.ComunidadInvitacion;
import com.ista.springboot.web.app.models.entity.Usuario;

@Service
public class ComunidadInvitacionService {

  private static final SecureRandom RNG = new SecureRandom();

  @Autowired private IComunidadInvitacion invitacionDao;

  public String generarTokenSeguro() {
    byte[] bytes = new byte[32]; // 256 bits
    RNG.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  @Transactional
  public ComunidadInvitacion crearInvitacion1Uso(Comunidad comunidad, Usuario usuario, Usuario admin, int horasExpira) {
    ComunidadInvitacion inv = new ComunidadInvitacion();
    inv.setComunidad(comunidad);
    inv.setUsuario(usuario);
    inv.setToken(generarTokenSeguro());
    inv.setEstado("activa");
    inv.setExpiresAt(OffsetDateTime.now().plusHours(horasExpira));
    inv.setCreatedBy(admin);
    return invitacionDao.save(inv);
  }

  @Transactional
  public ComunidadInvitacion validarTokenOrThrow(String token, Long usuarioId) {
    ComunidadInvitacion inv = invitacionDao.findByToken(token)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token inválido"));

    if (!"activa".equalsIgnoreCase(inv.getEstado())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token no está activo");
    }

    if (inv.getExpiresAt() != null && OffsetDateTime.now().isAfter(inv.getExpiresAt())) {
      inv.setEstado("expirada");
      invitacionDao.save(inv);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expirado");
    }

    if (inv.getUsuario() == null || inv.getUsuario().getId() == null
        || !inv.getUsuario().getId().equals(usuarioId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token no corresponde a este usuario");
    }

    return inv;
  }

  @Transactional
  public void marcarUsada(ComunidadInvitacion inv) {
    inv.setEstado("usada");
    inv.setUsedAt(OffsetDateTime.now());
    invitacionDao.save(inv);
  }
}
