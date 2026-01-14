// ============================================================
// UsuarioComunidadServiceImpl.java (COMPLETO + método NUEVO "unirsePorCodigo")
// ============================================================
package com.ista.springboot.web.app.models.services;

import java.time.OffsetDateTime;
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
    public static final String ESTADO_EXPULSADO = "expulsado";

    // ========= Tipos notificación (data FCM) =========
    private static final String TIPO_JOIN_REQUEST  = "JOIN_REQUEST";
    private static final String TIPO_JOIN_APPROVED = "JOIN_APPROVED";
    private static final String TIPO_JOIN_REJECTED = "JOIN_REJECTED";

    @Autowired private IUsuarioComunidad usuarioComunidadDao;
    @Autowired private IComunidadService comunidadService;
    @Autowired private IUsuario usuarioDao;
    @Autowired private ComunidadInvitacionService invitacionService;

    @Autowired private FirebaseMessagingService firebaseMessagingService;

    // ============================================================
    // ✅ Unirse por TOKEN (LEGACY)
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

        UsuarioComunidad existente = usuarioComunidadDao.findByUsuarioIdAndComunidadId(usuarioId, comunidad.getId()).orElse(null);
        if (existente != null) {
            if (ESTADO_ACTIVO.equalsIgnoreCase(existente.getEstado())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya perteneces a esta comunidad");
            }
            existente.setEstado(ESTADO_ACTIVO);
            existente.setRol(ROL_USER);
            if (inv.getCreatedBy() != null) existente.setAprobadoPor(inv.getCreatedBy());
            UsuarioComunidad actualizado = usuarioComunidadDao.save(existente);
            invitacionService.marcarUsada(inv);
            return actualizado;
        }

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
    // ✅ Unirse por CÓDIGO (LEGACY)  -> ya lo tenías
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
    // ✅ NUEVO: Unirse por CÓDIGO (PRO) - reusa el legacy pero corrige casos:
    //    - Si ya existe y está PENDIENTE -> lo activa
    //    - Si existe y está EXPULSADO -> 403 (o puedes permitir reactivar si quieres)
    //    - Si ya está ACTIVO -> 409
    // ============================================================
    @Override
    @Transactional
    public UsuarioComunidad unirsePorCodigo(Long usuarioId, String codigoAcceso) {

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

        UsuarioComunidad existente = usuarioComunidadDao.findByUsuarioIdAndComunidadId(usuarioId, comunidad.getId()).orElse(null);

        // Ya existe relación
        if (existente != null) {

            if (ESTADO_ACTIVO.equalsIgnoreCase(existente.getEstado())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya perteneces a esta comunidad");
            }

            if (ESTADO_EXPULSADO.equalsIgnoreCase(existente.getEstado())) {
                // Política: expulsado NO se reactiva por código
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes unirte porque fuiste expulsado");
            }

            // Si estaba PENDIENTE (u otro), lo activamos directamente por código
            existente.setEstado(ESTADO_ACTIVO);
            existente.setRol(ROL_USER);
            existente.setFechaUnion(OffsetDateTime.now());
            existente.setAprobadoPor(null); // por código, no por admin (si quieres puedes guardar un "system user")

            return usuarioComunidadDao.save(existente);
        }

        // No existe relación -> crear y activar
        UsuarioComunidad uc = new UsuarioComunidad();
        uc.setUsuario(usuario);
        uc.setComunidad(comunidad);
        uc.setRol(ROL_USER);
        uc.setEstado(ESTADO_ACTIVO);
        uc.setFechaUnion(OffsetDateTime.now());
        uc.setAprobadoPor(null);

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
    // ✅ NUEVO: Usuario solicita unirse (PENDIENTE) + NOTIFICA ADMINS
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
        UsuarioComunidad guardada;

        if (existente != null) {
            if (ESTADO_ACTIVO.equalsIgnoreCase(existente.getEstado())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya perteneces a esta comunidad");
            }
            if (ESTADO_PENDIENTE.equalsIgnoreCase(existente.getEstado())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya tienes una solicitud pendiente");
            }
            existente.setEstado(ESTADO_PENDIENTE);
            existente.setRol(ROL_USER);
            existente.setAprobadoPor(null);
            guardada = usuarioComunidadDao.save(existente);
        } else {
            UsuarioComunidad uc = new UsuarioComunidad();
            uc.setUsuario(usuario);
            uc.setComunidad(comunidad);
            uc.setRol(ROL_USER);
            uc.setEstado(ESTADO_PENDIENTE);
            guardada = usuarioComunidadDao.save(uc);
        }

        // ✅ Notificar a TODOS los admins activos de la comunidad
        notificarAdminsSolicitud(comunidadId, usuario);

        return guardada;
    }

    private void notificarAdminsSolicitud(Long comunidadId, Usuario solicitante) {
        try {
            List<UsuarioComunidad> miembros = usuarioComunidadDao.findByComunidadIdAndEstadoIgnoreCase(comunidadId, ESTADO_ACTIVO);

            for (UsuarioComunidad uc : miembros) {
                if (uc == null || uc.getUsuario() == null) continue;
                if (uc.getRol() == null || !ROL_ADMIN_COMUNIDAD.equalsIgnoreCase(uc.getRol())) continue;

                Usuario admin = uc.getUsuario();
                if (admin.getFcmToken() == null || admin.getFcmToken().isBlank()) continue;

                String titulo = "Solicitud para unirse";
                String cuerpo = (solicitante != null && solicitante.getNombre() != null && !solicitante.getNombre().isBlank())
                        ? "El usuario " + solicitante.getNombre() + " solicitó unirse a tu comunidad."
                        : "Un usuario solicitó unirse a tu comunidad.";

                Map<String, String> data = new HashMap<>();
                data.put("tipoNotificacion", TIPO_JOIN_REQUEST);
                data.put("comunidadId", comunidadId.toString());
                if (solicitante != null && solicitante.getId() != null) {
                    data.put("usuarioId", solicitante.getId().toString());
                }

                firebaseMessagingService.enviarNotificacionAToken(admin.getFcmToken(), titulo, cuerpo, data);
            }
        } catch (Exception ex) {
            System.out.println("ERROR notificando admins solicitud: " + ex.getMessage());
        }
    }

    // ============================================================
    // ✅ Admin lista solicitudes pendientes
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public List<UsuarioComunidad> listarSolicitudesPendientes(Long adminId, Long comunidadId) {
        requireAdminComunidad(adminId, comunidadId);
        return usuarioComunidadDao.findByComunidadIdAndEstadoIgnoreCase(comunidadId, ESTADO_PENDIENTE);
    }

    // ============================================================
    // ✅ Admin aprueba solicitud -> ACTIVO + NOTIFICA USUARIO
    // ============================================================
    @Override
    @Transactional
    public UsuarioComunidad aprobarSolicitud(Long adminId, Long comunidadId, Long usuarioId) {

        requireAdminComunidad(adminId, comunidadId);

        if (usuarioId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usuarioId requerido");

        UsuarioComunidad solicitud = usuarioComunidadDao.findByUsuarioIdAndComunidadId(usuarioId, comunidadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        if (!ESTADO_PENDIENTE.equalsIgnoreCase(solicitud.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La solicitud no está en estado pendiente");
        }

        Usuario admin = usuarioDao.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin no encontrado"));

        solicitud.setAprobadoPor(admin);
        solicitud.setEstado(ESTADO_ACTIVO);
        solicitud.setRol(ROL_USER);
        solicitud.setFechaUnion(OffsetDateTime.now());

        UsuarioComunidad guardada = usuarioComunidadDao.save(solicitud);

        notificarUsuarioAprobado(guardada);

        return guardada;
    }

    private void notificarUsuarioAprobado(UsuarioComunidad uc) {
        try {
            if (uc == null || uc.getUsuario() == null || uc.getComunidad() == null) return;

            Usuario u = uc.getUsuario();
            Comunidad c = uc.getComunidad();

            if (u.getFcmToken() == null || u.getFcmToken().isBlank()) return;

            String titulo = "Solicitud aprobada";
            String cuerpo  = "Ya puedes ingresar a la comunidad \"" + c.getNombre() + "\".";

            Map<String, String> data = new HashMap<>();
            data.put("tipoNotificacion", TIPO_JOIN_APPROVED);
            data.put("comunidadId", c.getId().toString());

            firebaseMessagingService.enviarNotificacionAToken(u.getFcmToken(), titulo, cuerpo, data);
        } catch (Exception ex) {
            System.out.println("ERROR notificando usuario aprobado: " + ex.getMessage());
        }
    }

    // ============================================================
    // ✅ Admin rechaza solicitud -> EXPULSADO + NOTIFICA usuario
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

        solicitud.setEstado(ESTADO_EXPULSADO);
        usuarioComunidadDao.save(solicitud);

        notificarUsuarioRechazado(solicitud);
    }

    private void notificarUsuarioRechazado(UsuarioComunidad uc) {
        try {
            if (uc == null || uc.getUsuario() == null || uc.getComunidad() == null) return;

            Usuario u = uc.getUsuario();
            Comunidad c = uc.getComunidad();

            if (u.getFcmToken() == null || u.getFcmToken().isBlank()) return;

            String titulo = "Solicitud rechazada";
            String cuerpo  = "Tu solicitud para unirte a \"" + c.getNombre() + "\" fue rechazada.";

            Map<String, String> data = new HashMap<>();
            data.put("tipoNotificacion", TIPO_JOIN_REJECTED);
            data.put("comunidadId", c.getId().toString());

            firebaseMessagingService.enviarNotificacionAToken(u.getFcmToken(), titulo, cuerpo, data);
        } catch (Exception ex) {
            System.out.println("ERROR notificando usuario rechazado: " + ex.getMessage());
        }
    }

    // ============================================================
    // ✅ MÉTODO LEGACY (compatibilidad token)
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
        Usuario usuario = solicitud.getUsuario();

        Usuario admin = usuarioDao.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin no encontrado"));

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
}
