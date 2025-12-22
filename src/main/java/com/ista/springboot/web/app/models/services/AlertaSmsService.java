package com.ista.springboot.web.app.models.services;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ista.springboot.web.app.models.entity.ContactoEmergencia;
import com.ista.springboot.web.app.models.entity.Incidente;

@Service
public class AlertaSmsService {

    private final IContactoEmergenciaService contactoService;
    private final TwilioSmsService twilioSmsService;

    public AlertaSmsService(IContactoEmergenciaService contactoService,
                            TwilioSmsService twilioSmsService) {
        this.contactoService = contactoService;
        this.twilioSmsService = twilioSmsService;
    }

    // 🔥 ASYNC para no bloquear el POST /incidentes
    @Async
    public void enviarSmsAContactosDelUsuario(Long usuarioId, Incidente incidente) {

        if (usuarioId == null || incidente == null) return;

        List<ContactoEmergencia> contactos =
                contactoService.findActivosByUsuarioId(usuarioId);

        if (contactos.isEmpty()) return;

        String mensaje = construirMensaje(incidente);

        for (ContactoEmergencia c : contactos) {
            String to = normalizarE164Ecuador(c.getTelefono());
            if (to == null) continue;

            try {
                twilioSmsService.enviarSms(to, mensaje);
            } catch (Exception e) {
                // ⚠️ No romper el flujo por un contacto fallido
                System.err.println("SMS falló a " + to + ": " + e.getMessage());
            }
        }
    }

    private String construirMensaje(Incidente inc) {
        String tipo = inc.getTipo() != null ? inc.getTipo() : "INCIDENTE";
        String prioridad = inc.getNivelPrioridad() != null ? inc.getNivelPrioridad() : "ALTA";

        String ubicacion = "";
        if (inc.getUbicacion() != null) {
            double lat = inc.getUbicacion().getY();
            double lng = inc.getUbicacion().getX();
            ubicacion = " https://www.openstreetmap.org/?mlat=" + lat + "&mlon=" + lng
                      + "#map=18/" + lat + "/" + lng;
        }

        String desc = inc.getDescripcion() != null ? inc.getDescripcion() + ". " : "";

        return "🚨 ALERTA SafeZone: " + tipo
                + " (Prioridad " + prioridad + "). "
                + desc
                + ubicacion;
    }

    private String normalizarE164Ecuador(String input) {
        if (input == null) return null;

        String s = input.replaceAll("[^0-9+]", "");

        if (s.startsWith("+") && s.matches("^\\+\\d{10,15}$")) return s;
        if (s.matches("^0\\d{9}$")) return "+593" + s.substring(1);
        if (s.matches("^\\d{9}$")) return "+593" + s;

        return null;
    }
}
