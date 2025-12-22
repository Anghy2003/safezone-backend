
package com.ista.springboot.web.app.models.services;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ista.springboot.web.app.models.entity.Comunidad;
import com.ista.springboot.web.app.models.entity.Incidente;
import com.ista.springboot.web.app.models.entity.Notificacion;
import com.ista.springboot.web.app.models.entity.Usuario;
import com.ista.springboot.web.app.models.entity.UsuarioComunidad;

@Service
public class NotificacionPushService {

    @Autowired private INotificacionService notificacionService;
    @Autowired private IComunidadService comunidadService;
    @Autowired private FirebaseMessagingService firebaseMessagingService;

    /**
     * Crea una Notificación en BD y envía FCM a todos los miembros de la comunidad (excepto el emisor).
     *
     * NOTA: con el modelo actual (una fila Notificacion para un broadcast),
     * el estado enviado/proveedorMsgId representa el último envío exitoso.
     * Si quieres "leído/enviado" por usuario, debes normalizar con tabla pivote (NotificacionUsuario).
     */
    public void notificarIncidente(Incidente incidente, Usuario emisor, String tipoNotificacion) {
        if (incidente == null || incidente.getId() == null) return;

        Comunidad comunidad = incidente.getComunidad();
        if (comunidad == null || comunidad.getId() == null) return;

        // Asegurar comunidad con membresías disponibles (evitar lazy issues)
        Comunidad comunidadFull = comunidadService.findById(comunidad.getId());
        if (comunidadFull == null || comunidadFull.getUsuarioComunidades() == null) return;

        // 1) Persistir notificación (evento)
        Notificacion noti = new Notificacion();
        noti.setUsuario(emisor);                 // "emisor" (según tu modelo actual)
        noti.setComunidad(comunidadFull);
        noti.setIncidente(incidente);
        noti.setTipoNotificacion(tipoNotificacion);

        final String titulo = resolveTitulo(tipoNotificacion);
        final String cuerpo = (incidente.getDescripcion() != null && !incidente.getDescripcion().isBlank())
                ? incidente.getDescripcion()
                : "Nueva alerta registrada";

        noti.setTitulo(titulo);
        noti.setMensaje(cuerpo);

        // Flags opcionales si quieres marcarlos desde el incidente
        noti.setTieneFoto(incidente.getImagenUrl() != null && !incidente.getImagenUrl().isBlank());
        noti.setTieneVideo(incidente.getVideoUrl() != null && !incidente.getVideoUrl().isBlank());
        noti.setTieneAudio(incidente.getAudioUrl() != null && !incidente.getAudioUrl().isBlank());
        // Si manejas ubicación en incidente como Point, aquí no la duplico (ya tienes lat/long en tabla Notificacion)

        Notificacion guardada = notificacionService.save(noti);

        // 2) Payload data para la app
        Map<String, String> data = new HashMap<>();
        data.put("tipoNotificacion", tipoNotificacion);
        data.put("incidenteId", incidente.getId().toString());
        data.put("comunidadId", comunidadFull.getId().toString());

        // 3) Enviar a miembros
        for (UsuarioComunidad uc : comunidadFull.getUsuarioComunidades()) {
            if (uc == null || uc.getUsuario() == null) continue;

            Usuario receptor = uc.getUsuario();
            if (receptor.getId() == null) continue;

            // No enviar al emisor
            if (emisor != null && emisor.getId() != null && receptor.getId().equals(emisor.getId())) continue;

            String token = receptor.getFcmToken();
            if (token == null || token.isBlank()) continue;

            try {
                String msgId = firebaseMessagingService.enviarNotificacionAToken(
                        token, titulo, cuerpo, data
                );

                // OJO: es una fila “broadcast”. Guardamos el último ok.
                guardada.setEnviado(true);
                guardada.setProveedorMsgId(msgId);
                guardada.setErrorEnvio(null);
                guardada.setFechaEnvio(OffsetDateTime.now());
                notificacionService.save(guardada);

            } catch (Exception ex) {
                guardada.setEnviado(false);
                guardada.setErrorEnvio(ex.getMessage());
                notificacionService.save(guardada);
            }
        }
    }

    private String resolveTitulo(String tipoNotificacion) {
        if (tipoNotificacion == null) return "Alerta";
        String t = tipoNotificacion.trim().toUpperCase();
        if (t.contains("VECINOS")) return "Emergencia cercana reportada";
        if (t.contains("COMUNIDAD")) return "Incidente en tu comunidad";
        return "Alerta";
    }
}
