package com.ista.springboot.web.app.models.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ista.springboot.web.app.models.dao.IUsuario;
import com.ista.springboot.web.app.models.dao.IUsuarioComunidad;
import com.ista.springboot.web.app.models.entity.Comunidad;
import com.ista.springboot.web.app.models.entity.ComunidadInvitacion;
import com.ista.springboot.web.app.models.entity.EstadoComunidad;
import com.ista.springboot.web.app.models.entity.Usuario;
import com.ista.springboot.web.app.models.entity.UsuarioComunidad;

@Service
public class UsuarioComunidadServiceImpl implements IUsuarioComunidadService {

    // ========= ROLES / ESTADOS (minúsculas) =========
    public static final String ROL_SUPER_ADMIN = "super_admin";
    public static final String ROL_ADMIN_COMUNIDAD = "admin_comunidad";
    public static final String ROL_USER = "usuario";

    public static final String ESTADO_ACTIVO = "activo";
    public static final String ESTADO_PENDIENTE = "pendiente";
    public static final String ESTADO_EXPULSADO = "expulsado"; // lo puedes reutilizar como “rechazado”

    @Autowired private IUsuarioComunidad usuarioComunidadDao;
    @Autowired private IComunidadService comunidadService;
    @Autowired private IUsuario usuarioDao;
    @Autowired private ComunidadInvitacionService invitacionService;

    // ============================================================
    // ✅ Unirse por TOKEN (seguro)
    // ============================================================
    @Override
    @Transactional
    public UsuarioComunidad unirUsuarioPorToken(Long usuarioId, String token) {

        if (usuarioId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usuarioId requerido");
        if (token == null || token.trim().isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "token requerido");

        Usuario usuario = usuarioDao.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        ComunidadInvitacion inv = invitacionService.validarTokenOrThrow(token.trim(), usuarioId);

        Comunidad comunidad = inv.getComunidad();
        if (comunidad == null || comunidad.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La invitación no tiene comunidad válida");
        }

        if (comunidad.getEstado() != EstadoComunidad.ACTIVA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La comunidad aún no está activa");
        }

        // Si ya existe cualquier relación, lo más correcto es:
        // - si está pendiente: pasar a activo
        // - si está activo: conflicto
        UsuarioComunidad existente = usuarioComunidadDao.findByUsuarioIdAndComunidadId(usuarioId, comunidad.getId()).orElse(null);
        if (existente != null) {
            if (ESTADO_ACTIVO.equalsIgnoreCase(existente.getEstado())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya perteneces a esta comunidad");
            }
            // pendiente -> activar
            existente.setEstado(ESTADO_ACTIVO);
            existente.setRol(ROL_USER);
            if (inv.getCreatedBy() != null) existente.setAprobadoPor(inv.getCreatedBy());
            UsuarioComunidad actualizado = usuarioComunidadDao.save(existente);
            invitacionService.marcarUsada(inv);
            return actualizado;
        }

        // No existía relación -> crear activo
        UsuarioComunidad uc = new UsuarioComunidad();
        uc.setUsuario(usuario);
        uc.setComunidad(comunidad);
        uc.setRol(ROL_USER);
        uc.setEstado(ESTADO_ACTIVO);
        if (inv.getCreatedBy() != null) uc.setAprobadoPor(inv.getCreatedBy());

        UsuarioComunidad saved = usuarioComunidadDao.save(uc);
        invitacionService.marcarUsada(inv);
        return saved;
    }

    // ============================================================
    // ✅ LEGACY: Unirse por CÓDIGO (si lo mantienes)
    // ============================================================
    @Override
    @Transactional
    public UsuarioComunidad unirUsuarioAComunidad(Long usuarioId, String codigoAcceso) {

        if (usuarioId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usuarioId requerido");
        if (codigoAcceso == null || codigoAcceso.trim().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "codigoAcceso requerido");

        Usuario usuario = usuarioDao.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Comunidad comunidad = comunidadService.findByCodigoAcceso(codigoAcceso.trim());
        if (comunidad == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comunidad no encontrada para ese código");

        if (comunidad.getEstado() != EstadoComunidad.ACTIVA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La comunidad aún no está activa");
        }

        if (usuarioComunidadDao.existsByUsuarioIdAndComunidadId(usuarioId, comunidad.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya perteneces a esta comunidad");
        }

        UsuarioComunidad uc = new UsuarioComunidad();
        uc.setUsuario(usuario);
        uc.setComunidad(comunidad);
        uc.setRol(ROL_USER);
        uc.setEstado(ESTADO_ACTIVO);

        return usuarioComunidadDao.save(uc);
    }

    // ============================================================
    // ✅ Validar ADMIN de ESA comunidad
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public void requireAdminComunidad(Long adminId, Long comunidadId) {

        if (adminId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "adminId requerido");
        if (comunidadId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "comunidadId requerido");

        boolean ok = usuarioComunidadDao.existsByUsuarioIdAndComunidadIdAndRolIgnoreCaseAndEstadoIgnoreCase(
                adminId, comunidadId, ROL_ADMIN_COMUNIDAD, ESTADO_ACTIVO
        );

        if (!ok) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere ADMIN de la comunidad");
    }

    // ============================================================
    // ✅ NUEVO: Usuario solicita unirse (queda PENDIENTE)
    // ============================================================
    @Override
    @Transactional
    public UsuarioComunidad solicitarUnirse(Long usuarioId, Long comunidadId) {

        if (usuarioId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usuarioId requerido");
        if (comunidadId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "comunidadId requerido");

        Usuario usuario = usuarioDao.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Comunidad comunidad = comunidadService.findById(comunidadId);
        if (comunidad == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comunidad no encontrada");

        if (comunidad.getEstado() != EstadoComunidad.ACTIVA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La comunidad aún no está activa");
        }

        UsuarioComunidad existente = usuarioComunidadDao.findByUsuarioIdAndComunidadId(usuarioId, comunidadId).orElse(null);
        if (existente != null) {
            if (ESTADO_ACTIVO.equalsIgnoreCase(existente.getEstado())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya perteneces a esta comunidad");
            }
            if (ESTADO_PENDIENTE.equalsIgnoreCase(existente.getEstado())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya tienes una solicitud pendiente");
            }
            // Si estaba expulsado/rechazado y quieres permitir re-solicitud:
            existente.setEstado(ESTADO_PENDIENTE);
            existente.setRol(ROL_USER);
            existente.setAprobadoPor(null);
            return usuarioComunidadDao.save(existente);
        }

        UsuarioComunidad uc = new UsuarioComunidad();
        uc.setUsuario(usuario);
        uc.setComunidad(comunidad);
        uc.setRol(ROL_USER);
        uc.setEstado(ESTADO_PENDIENTE);

        return usuarioComunidadDao.save(uc);
    }

    // ============================================================
    // ✅ NUEVO: Admin lista solicitudes pendientes de SU comunidad
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public List<UsuarioComunidad> listarSolicitudesPendientes(Long adminId, Long comunidadId) {
        requireAdminComunidad(adminId, comunidadId);
        return usuarioComunidadDao.findByComunidadIdAndEstadoIgnoreCase(comunidadId, ESTADO_PENDIENTE);
    }

    // ============================================================
    // ✅ NUEVO: Admin aprueba solicitud -> genera token 1-uso
    // ============================================================
    @Override
    @Transactional
    public Map<String, Object> aprobarSolicitudYGenerarToken(Long adminId, Long comunidadId, Long usuarioId, Integer horasExpira) {

        requireAdminComunidad(adminId, comunidadId);

        if (usuarioId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usuarioId requerido");
        int exp = (horasExpira == null || horasExpira <= 0) ? 24 : horasExpira;

        UsuarioComunidad solicitud = usuarioComunidadDao.findByUsuarioIdAndComunidadId(usuarioId, comunidadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        if (!ESTADO_PENDIENTE.equalsIgnoreCase(solicitud.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La solicitud no está en estado pendiente");
        }

        Comunidad comunidad = solicitud.getComunidad();
        if (comunidad == null || comunidad.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solicitud sin comunidad válida");
        }

        Usuario usuario = solicitud.getUsuario();
        if (usuario == null || usuario.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solicitud sin usuario válido");
        }

        Usuario admin = usuarioDao.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin no encontrado"));

        // Marcar quién aprueba (auditoría)
        solicitud.setAprobadoPor(admin);
        usuarioComunidadDao.save(solicitud);

        ComunidadInvitacion inv = invitacionService.crearInvitacion1Uso(comunidad, usuario, admin, exp);

        Map<String, Object> out = new HashMap<>();
        out.put("success", true);
        out.put("comunidadId", comunidadId);
        out.put("usuarioId", usuarioId);
        out.put("token", inv.getToken());
        out.put("expiresAt", inv.getExpiresAt() == null ? null : inv.getExpiresAt().toString());
        out.put("estado", inv.getEstado());
        return out;
    }

    // ============================================================
    // ✅ NUEVO: Admin rechaza solicitud
    // ============================================================
    @Override
    @Transactional
    public void rechazarSolicitud(Long adminId, Long comunidadId, Long usuarioId) {
        requireAdminComunidad(adminId, comunidadId);

        if (usuarioId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usuarioId requerido");

        UsuarioComunidad solicitud = usuarioComunidadDao.findByUsuarioIdAndComunidadId(usuarioId, comunidadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        if (!ESTADO_PENDIENTE.equalsIgnoreCase(solicitud.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La solicitud no está en estado pendiente");
        }

        // Opción mínima: marcar como expulsado (o crea “rechazado” si quieres más fino)
        solicitud.setEstado(ESTADO_EXPULSADO);
        usuarioComunidadDao.save(solicitud);
    }
}
