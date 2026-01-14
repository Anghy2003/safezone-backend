// =====================================================
// ComunidadServiceImpl.java  (AJUSTADO: vuelve SMS Twilio con código al aprobar)
// =====================================================
package com.ista.springboot.web.app.models.services;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ista.springboot.web.app.models.dao.IComunidad;
import com.ista.springboot.web.app.models.dao.IUsuario;
import com.ista.springboot.web.app.models.dao.IUsuarioComunidad;
import com.ista.springboot.web.app.models.entity.Comunidad;
import com.ista.springboot.web.app.models.entity.EstadoComunidad;
import com.ista.springboot.web.app.models.entity.Usuario;
import com.ista.springboot.web.app.models.entity.UsuarioComunidad;
import com.ista.springboot.web.app.repository.ComunidadFetchRepository;

@Service
public class ComunidadServiceImpl implements IComunidadService {

    @Autowired private IComunidad comunidadDao;
    @Autowired private IUsuarioComunidad usuarioComunidadDao;
    @Autowired private ComunidadFetchRepository comunidadFetchRepository;
    @Autowired private IUsuario usuarioDao;

    // ✅ FCM (para notificar “ya eres admin”)
    @Autowired(required = false)
    private FirebaseMessagingService firebaseMessagingService;

    // ✅ Twilio SMS (para enviar código al aprobar)
    @Autowired(required = false)
    private TwilioSmsService twilioSmsService;

    // ===================== LISTAR =====================
    @Override
    public List<Comunidad> findAll() {
        List<Comunidad> comunidades = (List<Comunidad>) comunidadDao.findAll();
        for (Comunidad c : comunidades) {
            long count = usuarioComunidadDao.countByComunidadId(c.getId());
            c.setMiembrosCount(count);
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

    // ✅ Solo referencial (no para unirse) - pero tu app puede usarlo como lookup previo
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

    // ===================== SOLICITAR =====================
    @Override
    public Comunidad solicitarComunidad(Comunidad comunidad, Long usuarioId) {
        comunidad.setId(null);
        comunidad.setEstado(EstadoComunidad.SOLICITADA);
        comunidad.setActiva(false);

        // ✅ el código se genera al aprobar
        comunidad.setCodigoAcceso(null);

        comunidad.setSolicitadaPorUsuarioId(usuarioId);
        return comunidadDao.save(comunidad);
    }

    // ===================== APROBAR =====================
    // ✅ Al aprobar: activa + genera código + solicitante => admin_comunidad activo
    @Override
    @Transactional
    public Comunidad aprobarComunidad(Long comunidadId) {

        Comunidad comunidad = comunidadDao.findById(comunidadId)
                .orElseThrow(() -> new RuntimeException("Comunidad no encontrada"));

        if (comunidad.getEstado() != EstadoComunidad.SOLICITADA) {
            throw new RuntimeException("La comunidad no está en estado SOLICITADA");
        }

        // ✅ activar
        comunidad.setEstado(EstadoComunidad.ACTIVA);
        comunidad.setActiva(true);

        // ✅ Generar código
        if (comunidad.getCodigoAcceso() == null || comunidad.getCodigoAcceso().isBlank()) {
            comunidad.setCodigoAcceso(generarCodigo5());
        }

        Comunidad guardada = comunidadDao.save(comunidad);

        // ✅ solicitante => admin_comunidad + activo (crear o actualizar)
        Long solicitanteId = guardada.getSolicitadaPorUsuarioId();
        if (solicitanteId != null) {
            Usuario solicitante = usuarioDao.findById(solicitanteId).orElse(null);
            if (solicitante != null) {

                UsuarioComunidad uc = usuarioComunidadDao
                        .findByUsuarioIdAndComunidadId(solicitanteId, guardada.getId())
                        .orElse(null);

                if (uc == null) {
                    uc = new UsuarioComunidad();
                    uc.setUsuario(solicitante);
                    uc.setComunidad(guardada);
                }

                uc.setRol(UsuarioComunidadServiceImpl.ROL_ADMIN_COMUNIDAD);
                uc.setEstado(UsuarioComunidadServiceImpl.ESTADO_ACTIVO);
                uc.setFechaUnion(OffsetDateTime.now());

                usuarioComunidadDao.save(uc);

                // ✅ SMS con código (Twilio) - vuelve el comportamiento “antes”
                enviarSmsCodigoComunidad(guardada, solicitante);

                // ✅ FCM (si existe)
                notificarSolicitanteAprobacion(guardada, solicitante);
            }
        }

        return guardada;
    }

    // =====================================================
    // SMS: Enviar código por Twilio al solicitante
    // =====================================================
    private void enviarSmsCodigoComunidad(Comunidad comunidad, Usuario solicitante) {
        try {
            if (twilioSmsService == null) return;
            if (solicitante == null) return;

            // AJUSTA este getter según tu entidad Usuario:
            // - getTelefono()
            // - getCelular()
            // - getPhone()
            // etc.
            String telefono = solicitante.getTelefono();

            if (telefono == null || telefono.isBlank()) return;

            String codigo = comunidad.getCodigoAcceso();
            if (codigo == null || codigo.isBlank()) return;

            String msg = "SafeZone: Tu comunidad \"" + comunidad.getNombre() + "\" fue aprobada.\n"
                       + "Código de acceso: " + codigo + "\n"
                       + "Si alguien no lo sabe, puede solicitar unirse desde la app.";

            // Debe venir en formato +593...
            String sid = twilioSmsService.enviarSms(telefono.trim(), msg);
            System.out.println("SMS Twilio enviado. SID=" + sid);

        } catch (Exception ex) {
            System.out.println("ERROR enviando SMS Twilio: " + ex.getMessage());
        }
    }

    private void notificarSolicitanteAprobacion(Comunidad comunidad, Usuario solicitante) {
        try {
            if (firebaseMessagingService == null) return;
            if (solicitante == null || solicitante.getFcmToken() == null || solicitante.getFcmToken().isBlank()) return;

            String titulo = "Comunidad aprobada";
            String cuerpo  = "Tu comunidad \"" + comunidad.getNombre() + "\" fue aprobada. Ya eres administrador.";

            java.util.Map<String, String> data = new java.util.HashMap<>();
            data.put("tipoNotificacion", "COMMUNITY_APPROVED");
            data.put("comunidadId", comunidad.getId().toString());
            if (comunidad.getCodigoAcceso() != null) {
                data.put("codigoAcceso", comunidad.getCodigoAcceso());
            }

            firebaseMessagingService.enviarNotificacionAToken(
                    solicitante.getFcmToken(),
                    titulo,
                    cuerpo,
                    data
            );
        } catch (Exception ex) {
            System.out.println("ERROR notificando aprobación de comunidad: " + ex.getMessage());
        }
    }

    private String generarCodigo5() {
        Random r = new Random();
        int numero = 10000 + r.nextInt(90000);
        return String.valueOf(numero);
    }

    @Override
    public Comunidad findByIdWithMiembrosActivos(Long id) {
        return comunidadFetchRepository.findByIdWithMiembrosActivos(id).orElse(null);
    }
}
