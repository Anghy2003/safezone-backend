// package com.ista.springboot.web.app.models.services;

package com.ista.springboot.web.app.models.services;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ista.springboot.web.app.models.entity.ContactoEmergencia;
import com.ista.springboot.web.app.models.entity.Incidente;
import com.ista.springboot.web.app.models.entity.Usuario;

@Service
public class AlertaSmsService {

    private final IContactoEmergenciaService contactoService;
    private final TwilioSmsService twilioSmsService;

    // ✅ NUEVO: para poder incluir nombre del usuario en el SMS
    private final IUsuarioService usuarioService;

    public AlertaSmsService(
            IContactoEmergenciaService contactoService,
            TwilioSmsService twilioSmsService,
            IUsuarioService usuarioService
    ) {
        this.contactoService = contactoService;
        this.twilioSmsService = twilioSmsService;
        this.usuarioService = usuarioService;
    }

    // 🔥 ASYNC para no bloquear el POST /incidentes
    @Async
    public void enviarSmsAContactosDelUsuario(Long usuarioId, Incidente incidente) {

        if (usuarioId == null || incidente == null) return;

        List<ContactoEmergencia> contactos =
                contactoService.findActivosByUsuarioId(usuarioId);

        if (contactos == null || contactos.isEmpty()) return;

        String mensaje = construirMensaje(usuarioId, incidente);

        for (ContactoEmergencia c : contactos) {
            if (c == null) continue;

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

    private String construirMensaje(Long usuarioId, Incidente inc) {

        // Nombre del usuario (para "quién reporta")
        String nombre = "Usuario";
        try {
            if (usuarioId != null) {
                Usuario u = usuarioService.findById(usuarioId);
                if (u != null && u.getNombre() != null && !u.getNombre().isBlank()) {
                    nombre = u.getNombre().trim();
                }
            }
        } catch (Exception ignored) {
            // si falla la consulta, no rompemos el SMS
        }

        String tipo = (inc.getTipo() != null && !inc.getTipo().isBlank())
                ? inc.getTipo().trim()
                : "INCIDENTE";

        String prioridad = (inc.getNivelPrioridad() != null && !inc.getNivelPrioridad().isBlank())
                ? inc.getNivelPrioridad().trim()
                : "ALTA";

        String desc = (inc.getDescripcion() != null && !inc.getDescripcion().isBlank())
                ? inc.getDescripcion().trim()
                : "Sin descripción";

        String ubicacion = "";
        if (inc.getUbicacion() != null) {
            double lat = inc.getUbicacion().getY();
            double lng = inc.getUbicacion().getX();
            ubicacion = " https://www.openstreetmap.org/?mlat=" + lat + "&mlon=" + lng
                      + "#map=18/" + lat + "/" + lng;
        }

        // ✅ Formato claro y “con más info”
        return "🚨 SafeZone ALERTA\n"
                + "Usuario: " + nombre + "\n"
                + "Qué pasó: " + desc + "\n"
                + "Tipo: " + tipo + " (Prioridad " + prioridad + ")\n"
                + "Ubicación:" + ubicacion;
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
