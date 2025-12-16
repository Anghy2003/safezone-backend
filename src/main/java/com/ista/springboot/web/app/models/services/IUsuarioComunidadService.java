package com.ista.springboot.web.app.models.services;

import com.ista.springboot.web.app.models.entity.UsuarioComunidad;

public interface IUsuarioComunidadService {

    UsuarioComunidad unirUsuarioAComunidad(Long usuarioId, String codigoAcceso);

}
