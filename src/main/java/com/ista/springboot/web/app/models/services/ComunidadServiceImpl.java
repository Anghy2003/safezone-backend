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

    // ✅ Eliminado lógico recomendado desde controller (suspender)
    @Override
    public void delete(Long id) {
        comunidadDao.deleteById(id);
    }

    // ✅ Solo referencial (no para unirse)
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

        // ✅ Código NO se usa aquí. Se genera al aprobar (solo referencial).
        comunidad.setCodigoAcceso(null);

        comunidad.setSolicitadaPorUsuarioId(usuarioId);
        return comunidadDao.save(comunidad);
    }

    // ===================== APROBAR =====================
    // ✅ Al aprobar: activa + genera código (referencial) + solicitante => admin_comunidad activo
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

        // ✅ Código referencial (NO para unirse)
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

                // 🔥 CONSISTENCIA total con tu UsuarioComunidadServiceImpl
                uc.setRol(UsuarioComunidadServiceImpl.ROL_ADMIN_COMUNIDAD);
                uc.setEstado(UsuarioComunidadServiceImpl.ESTADO_ACTIVO);
                uc.setFechaUnion(OffsetDateTime.now());

                usuarioComunidadDao.save(uc);

                // ✅ Notificar por FCM (si existe el bean)
                notificarSolicitanteAprobacion(guardada, solicitante);
            }
        }

        return guardada;
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
                data.put("codigoReferencial", comunidad.getCodigoAcceso()); // ✅ solo referencial
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
