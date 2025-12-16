package com.ista.springboot.web.app.models.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ista.springboot.web.app.models.dao.IUsuarioComunidad;
import com.ista.springboot.web.app.models.dao.IUsuario;
import com.ista.springboot.web.app.models.entity.Comunidad;
import com.ista.springboot.web.app.models.entity.EstadoComunidad;
import com.ista.springboot.web.app.models.entity.Usuario;
import com.ista.springboot.web.app.models.entity.UsuarioComunidad;

@Service
public class UsuarioComunidadServiceImpl implements IUsuarioComunidadService {

    @Autowired
    private IUsuarioComunidad usuarioComunidadDao;

    @Autowired
    private IComunidadService comunidadService;

    @Autowired
    private IUsuario usuarioDao; // DAO de Usuario

    @Override
    @Transactional
    public UsuarioComunidad unirUsuarioAComunidad(Long usuarioId, String codigoAcceso) {

        // 1) Buscar usuario
        Usuario usuario = usuarioDao.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // 2) Verificar si YA pertenece a alguna comunidad
        //    Regla: un usuario solo puede estar en UNA comunidad, nunca en dos.
        if (usuarioComunidadDao.existsByUsuarioId(usuario.getId())) {
            // Puedes recuperar la relación actual para dar más contexto
            UsuarioComunidad actual = usuarioComunidadDao
                    .findFirstByUsuarioId(usuario.getId())
                    .orElse(null);

            String detalle = "";
            if (actual != null && actual.getComunidad() != null) {
                detalle = " (ya pertenece a la comunidad: "
                        + actual.getComunidad().getNombre()
                        + " - ID " + actual.getComunidad().getId() + ")";
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El usuario ya pertenece a una comunidad" + detalle
            );
        }

        // 3) Buscar comunidad por código
        Comunidad comunidad = comunidadService.findByCodigoAcceso(codigoAcceso);
        if (comunidad == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Comunidad no encontrada para ese código");
        }

        // 4) Solo permitir comunidades ACTIVAS
        if (comunidad.getEstado() != EstadoComunidad.ACTIVA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La comunidad aún no está activa");
        }

        // 5) Crear la relación usuario-comunidad (como vecino activo)
        UsuarioComunidad uc = new UsuarioComunidad();
        uc.setUsuario(usuario);
        uc.setComunidad(comunidad);
        uc.setRol("vecino");
        uc.setEstado("activo");

        return usuarioComunidadDao.save(uc);
    }

    // (opcional) método helper para consultar la comunidad actual del usuario
    @Transactional(readOnly = true)
    public UsuarioComunidad obtenerComunidadActual(Long usuarioId) {
        return usuarioComunidadDao.findFirstByUsuarioId(usuarioId).orElse(null);
    }
}
