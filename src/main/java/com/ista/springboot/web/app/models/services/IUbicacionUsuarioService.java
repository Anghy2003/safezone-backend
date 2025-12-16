package com.ista.springboot.web.app.models.services;

import java.util.List;

import com.ista.springboot.web.app.dto.UsuarioCercanoDTO;
import com.ista.springboot.web.app.models.entity.UbicacionUsuario;

public interface IUbicacionUsuarioService {

    List<UbicacionUsuario> findAll();

    UbicacionUsuario save(UbicacionUsuario ubicacion);

    UbicacionUsuario findById(Long id);

    UbicacionUsuario findByUsuarioId(Long usuarioId);

    List<UsuarioCercanoDTO> findNearby(double lat, double lng, double radio, int lastMinutes, int limit);

    void delete(Long id);
}
