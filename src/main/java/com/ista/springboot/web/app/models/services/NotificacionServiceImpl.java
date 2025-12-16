package com.ista.springboot.web.app.models.services;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.ista.springboot.web.app.models.dao.INotificacion;
import com.ista.springboot.web.app.models.entity.Comunidad;
import com.ista.springboot.web.app.models.entity.Notificacion;

@Service
public class NotificacionServiceImpl implements INotificacionService {

    @Autowired
    private INotificacion notificacionDao;

    @Autowired
    private FirebaseMessagingService firebaseMessagingService; // servicio FCM

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // 👈 WEBSOCKET

    @Override
    public List<Notificacion> findAll() {
        return (List<Notificacion>) notificacionDao.findAll();
    }

    @Override
    public Notificacion save(Notificacion notificacion) {

        // 👉 Primero guardar en BD
        Notificacion guardada = notificacionDao.save(notificacion);

        try {
            if (Boolean.TRUE.equals(guardada.getEnviado())) {
                return guardada; // ya fue enviada
            }

            String tipo = guardada.getTipoNotificacion();
            if (tipo == null) tipo = "";

            // ===================== OBTENER comunidadId =====================
            Long comunidadId = null;

            // A) Si viene por incidente → sacamos la comunidad del incidente
            if (guardada.getIncidente() != null &&
                guardada.getIncidente().getComunidad() != null) {

                comunidadId = guardada.getIncidente().getComunidad().getId();
            }
            // B) Si la notificación tiene comunidad directa (recomendado para chat)
            else if (guardada.getComunidad() != null) {
                Comunidad c = guardada.getComunidad();
                comunidadId = c.getId();
            }

            // ===================== DEFINIR TOPICS =====================
            String topicFCM = null;
            String topicWS  = null;

            if (comunidadId != null) {
                if (tipo.equals("INCIDENTE_COMUNIDAD") ||
                    tipo.equals("CHAT_COMUNIDAD")) {

                    topicFCM = "comunidad-" + comunidadId;
                    topicWS  = "/topic/comunidad-" + comunidadId;

                } else if (tipo.equals("INCIDENTE_VECINOS") ||
                           tipo.equals("CHAT_VECINOS")) {

                    topicFCM = "vecinos-" + comunidadId;
                    topicWS  = "/topic/vecinos-" + comunidadId;
                }
            }

            // ===================== ✉ PUSH FCM A TOPIC =====================
            if (topicFCM != null) {

                Map<String, String> data = new HashMap<>();
                data.put("tipo", tipo);
                data.put("notificacionId", guardada.getId().toString());
                if (comunidadId != null) {
                    data.put("comunidadId", comunidadId.toString());
                }

                String titulo = guardada.getTitulo() != null
                        ? guardada.getTitulo()
                        : "Alerta comunitaria";

                String cuerpo = guardada.getMensaje() != null
                        ? guardada.getMensaje()
                        : "";

                String messageId = firebaseMessagingService.enviarNotificacionATopic(
                        topicFCM,
                        titulo,
                        cuerpo,
                        data
                );

                guardada.setProveedorMsgId(messageId);
            }

            // ===================== 🌐 EMITIR MENSAJE POR WEBSOCKET =====================
            if (topicWS != null) {
                messagingTemplate.convertAndSend(topicWS, guardada);
            }

            // ===================== ACTUALIZAR ESTADO =====================
            guardada.setEnviado(true);
            guardada.setFechaEnvio(OffsetDateTime.now());
            guardada = notificacionDao.save(guardada);

        } catch (Exception e) {
            guardada.setErrorEnvio(e.getMessage());
            guardada.setEnviado(false);
            notificacionDao.save(guardada);
            e.printStackTrace();
        }

        return guardada;
    }

    @Override
    public Notificacion findById(Long id) {
        return notificacionDao.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        notificacionDao.deleteById(id);
    }
}
