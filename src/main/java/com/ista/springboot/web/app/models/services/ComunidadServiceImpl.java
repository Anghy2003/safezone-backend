package com.ista.springboot.web.app.models.services;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ista.springboot.web.app.models.dao.IComunidad;
import com.ista.springboot.web.app.models.dao.IUsuario;
import com.ista.springboot.web.app.models.dao.IUsuarioComunidad;
import com.ista.springboot.web.app.models.entity.Comunidad;
import com.ista.springboot.web.app.models.entity.EstadoComunidad;
import com.ista.springboot.web.app.models.entity.Usuario;
import com.ista.springboot.web.app.repository.ComunidadFetchRepository;

@Service
public class ComunidadServiceImpl implements IComunidadService {

    @Autowired
    private IComunidad comunidadDao;

    @Autowired
    private IUsuarioComunidad usuarioComunidadDao;

    @Autowired
    private ComunidadFetchRepository comunidadFetchRepository;

    // ✅ Para enviar SMS al solicitante
    @Autowired
    private IUsuario usuarioDao;

    @Autowired
    private TwilioSmsService smsService;

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

    // ✅ SOLICITAR comunidad: fotoUrl + centroGeografico + radioKm ya vienen seteados por controller
    @Override
    public Comunidad solicitarComunidad(Comunidad comunidad, Long usuarioId) {
        comunidad.setId(null);
        comunidad.setEstado(EstadoComunidad.SOLICITADA);
        comunidad.setActiva(false);
        comunidad.setCodigoAcceso(null);

        // ✅ guardar solicitante para enviar SMS luego
        comunidad.setSolicitadaPorUsuarioId(usuarioId);

        return comunidadDao.save(comunidad);
    }

    // ✅ APROBAR comunidad: genera código y envía SMS al solicitante
    @Override
    public Comunidad aprobarComunidad(Long comunidadId) {
        Comunidad comunidad = comunidadDao.findById(comunidadId)
            .orElseThrow(() -> new RuntimeException("Comunidad no encontrada"));

        if (comunidad.getEstado() != EstadoComunidad.SOLICITADA) {
            throw new RuntimeException("La comunidad no está en estado SOLICITADA");
        }

        comunidad.setEstado(EstadoComunidad.ACTIVA);
        comunidad.setActiva(true);
        comunidad.setCodigoAcceso(generarCodigo5());

        Comunidad guardada = comunidadDao.save(comunidad);

        // ✅ SMS
        enviarSmsCodigoSiAplica(guardada);

        return guardada;
    }

    private void enviarSmsCodigoSiAplica(Comunidad comunidad) {
        Long solicitanteId = comunidad.getSolicitadaPorUsuarioId();
        if (solicitanteId == null) return;

        Usuario u = usuarioDao.findById(solicitanteId).orElse(null);
        if (u == null) return;

        String telefono = u.getTelefono();
        if (telefono == null || telefono.trim().isEmpty()) return;

        // Normaliza si el usuario guarda 09xxxx
        String to = normalizarE164Ecuador(telefono);

        String msg = "SafeZone: Tu comunidad \"" + comunidad.getNombre()
            + "\" fue aprobada. Tu código de acceso es: "
            + comunidad.getCodigoAcceso()
            + ". Compártelo con tus vecinos.";

        try {
            smsService.enviarSms(to, msg);
        } catch (Exception ex) {
            // No bloquees la aprobación si falla Twilio
            System.out.println("No se pudo enviar SMS: " + ex.getMessage());
        }
    }

    private String normalizarE164Ecuador(String input) {
        String v = input.replace(" ", "").replace("-", "");
        if (v.startsWith("+")) return v;
        if (v.startsWith("09") && v.length() == 10) {
            return "+593" + v.substring(1); // 09xxxxxxxx -> +5939xxxxxxxx
        }
        if (v.startsWith("9") && v.length() == 9) {
            return "+593" + v; // 9xxxxxxxx -> +5939xxxxxxxx
        }
        return v;
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
