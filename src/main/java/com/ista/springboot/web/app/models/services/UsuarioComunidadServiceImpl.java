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

    // ✅ Admin “fijo” por correo
    private static final String ADMIN_EMAIL = "safezonecomunity@gmail.com";

    // ✅ ESTÁNDAR BD (minúsculas)
    private static final String ROL_ADMIN = "admin";
    private static final String ROL_USER  = "usuario"; // o "vecino" si decides mantenerlo
    private static final String ESTADO_ACTIVO = "activo";

    @Autowired
    private IUsuarioComunidad usuarioComunidadDao;

    @Autowired
    private IComunidadService comunidadService;

    @Autowired
    private IUsuario usuarioDao;

    private boolean isAdminEmail(String email) {
        return email != null && email.trim().equalsIgnoreCase(ADMIN_EMAIL);
    }

    @Override
    @Transactional
    public UsuarioComunidad unirUsuarioAComunidad(Long usuarioId, String codigoAcceso) {

        // 1) Buscar usuario
        Usuario usuario = usuarioDao.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // 2) Regla: un usuario solo puede estar en UNA comunidad
        if (usuarioComunidadDao.existsByUsuarioId(usuario.getId())) {
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

        // 5) Resolver rol automáticamente
        final boolean esAdmin = isAdminEmail(usuario.getEmail());
        final String rolAsignado = esAdmin ? ROL_ADMIN : ROL_USER;

        // 6) Admin único GLOBAL:
        // Si este usuario va a ser admin, verificamos si ya existe un admin diferente.
        if (ROL_ADMIN.equalsIgnoreCase(rolAsignado)) {
            UsuarioComunidad adminActual = usuarioComunidadDao
                    .findFirstByRolIgnoreCase(ROL_ADMIN)
                    .orElse(null);

            if (adminActual != null
                    && adminActual.getUsuario() != null
                    && !adminActual.getUsuario().getId().equals(usuario.getId())) {

                String extra = " (admin actual: " + adminActual.getUsuario().getEmail() + ")";
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Ya existe un admin global en el sistema" + extra
                );
            }
        }

        // 7) Crear relación usuario-comunidad
        UsuarioComunidad uc = new UsuarioComunidad();
        uc.setUsuario(usuario);
        uc.setComunidad(comunidad);
        uc.setRol(rolAsignado);         // ✅ "admin" o "usuario"
        uc.setEstado(ESTADO_ACTIVO);    // ✅ "activo"

        return usuarioComunidadDao.save(uc);
    }

    @Transactional(readOnly = true)
    public UsuarioComunidad obtenerComunidadActual(Long usuarioId) {
        return usuarioComunidadDao.findFirstByUsuarioId(usuarioId).orElse(null);
    }
}
