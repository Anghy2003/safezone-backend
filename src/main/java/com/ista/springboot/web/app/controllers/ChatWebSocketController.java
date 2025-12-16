package com.ista.springboot.web.app.controllers;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.ista.springboot.web.app.models.dto.ChatMessageDto;
import com.ista.springboot.web.app.models.entity.Comunidad;
import com.ista.springboot.web.app.models.entity.Notificacion;
import com.ista.springboot.web.app.models.entity.Usuario;
import com.ista.springboot.web.app.models.services.INotificacionService;
import com.ista.springboot.web.app.models.dao.IUsuario;

@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private IUsuario usuarioDao;

    @Autowired
    private INotificacionService notificacionService;


    // ============================ CHAT COMUNIDAD ============================
    @MessageMapping("/chat/comunidad")
    public void enviarMensajeComunidad(ChatMessageDto dto) {
        System.out.println("DEBUG WS /chat/comunidad → recibido DTO:");
        if (dto != null) {
            System.out.println("  usuarioId   = " + dto.getUsuarioId());
            System.out.println("  comunidadId = " + dto.getComunidadId());
            System.out.println("  contenido   = " + dto.getContenido());
        } else {
            System.out.println("  DTO ES NULL 🤯");
        }

        manejarMensajeChat(dto, "CHAT_COMUNIDAD");
    }

    // ============================ CHAT VECINOS ==============================
    @MessageMapping("/chat/vecinos")
    public void enviarMensajeVecinos(ChatMessageDto dto) {
        System.out.println("DEBUG WS /chat/vecinos → recibido DTO:");
        if (dto != null) {
            System.out.println("  usuarioId   = " + dto.getUsuarioId());
            System.out.println("  comunidadId = " + dto.getComunidadId());
            System.out.println("  contenido   = " + dto.getContenido());
        } else {
            System.out.println("  DTO ES NULL 🤯");
        }

        manejarMensajeChat(dto, "CHAT_VECINOS");
    }


    // ============================ LÓGICA GENERAL =============================
    private void manejarMensajeChat(ChatMessageDto dto, String tipoNotificacion) {

        System.out.println("DEBUG manejarMensajeChat() → tipo=" + tipoNotificacion);
        if (dto == null) {
            System.out.println("DEBUG manejarMensajeChat → DTO null, se aborta");
            return;
        }

        System.out.println("DEBUG manejarMensajeChat → usuarioId=" + dto.getUsuarioId()
                + ", comunidadId=" + dto.getComunidadId()
                + ", contenido=" + dto.getContenido());

        if (dto.getUsuarioId() == null || dto.getComunidadId() == null) {
            System.out.println("DEBUG manejarMensajeChat → usuarioId o comunidadId NULOS, se aborta");
            return; // podrías lanzar excepción si quieres
        }

        // 🔹 Cargar usuario real (evita entidades proxy)
        System.out.println("DEBUG manejarMensajeChat → buscando Usuario id=" + dto.getUsuarioId());
        Usuario usuario = usuarioDao.findById(dto.getUsuarioId()).orElse(null);
        if (usuario == null) {
            System.out.println("DEBUG manejarMensajeChat → Usuario no encontrado, se aborta");
            return;
        }
        System.out.println("DEBUG manejarMensajeChat → Usuario encontrado: "
                + usuario.getId() + " - " + usuario.getNombre());

        // 🔹 Crear solo referencia a comunidad (NO cargar entidad completa)
        Comunidad comunidad = new Comunidad();
        comunidad.setId(dto.getComunidadId());
        System.out.println("DEBUG manejarMensajeChat → Comunidad referida con id=" + dto.getComunidadId());

        // ===================== Crear Notificacion segura =====================
        Notificacion noti = new Notificacion();
        noti.setUsuario(usuario);
        noti.setComunidad(comunidad);
        noti.setIncidente(null);
        noti.setTipoNotificacion(tipoNotificacion);
        noti.setTitulo("Mensaje de chat");
        noti.setMensaje(dto.getContenido());
        noti.setLeido(false);
        noti.setEnviado(false);
        noti.setFechaEnvio(OffsetDateTime.now());

        System.out.println("DEBUG manejarMensajeChat → guardando Notificacion en BD...");
        Notificacion guardada = notificacionService.save(noti);
        System.out.println("DEBUG manejarMensajeChat → Notificacion guardada id="
                + guardada.getId() + ", tipo=" + guardada.getTipoNotificacion());

        // ================== Construir Payload seguro para WebSocket ==================
        Map<String, Object> usuarioJson = new HashMap<>();
        usuarioJson.put("id", usuario.getId());
        usuarioJson.put("nombre", usuario.getNombre()
                + (usuario.getApellido() != null ? " " + usuario.getApellido() : ""));
        usuarioJson.put("fotoUrl", usuario.getFotoUrl());

        Map<String, Object> payload = new HashMap<>();
        payload.put("tipoNotificacion", guardada.getTipoNotificacion());
        payload.put("mensaje", guardada.getMensaje());
        payload.put("fechaEnvio",
                guardada.getFechaEnvio() != null ? guardada.getFechaEnvio().toString() : null);
        payload.put("usuario", usuarioJson);
        payload.put("comunidadId", dto.getComunidadId()); // ⬅️ Necesario para Flutter

        System.out.println("DEBUG manejarMensajeChat → payload construido:");
        System.out.println("  tipoNotificacion=" + payload.get("tipoNotificacion"));
        System.out.println("  mensaje=" + payload.get("mensaje"));
        System.out.println("  fechaEnvio=" + payload.get("fechaEnvio"));
        System.out.println("  usuario.id=" + usuarioJson.get("id"));
        System.out.println("  comunidadId=" + payload.get("comunidadId"));

        // ======================= Enviar por WebSocket =======================
        Long comunidadId = dto.getComunidadId();

        if ("CHAT_COMUNIDAD".equals(tipoNotificacion)) {
            System.out.println("DEBUG manejarMensajeChat → enviando a /topic/comunidad-" + comunidadId);
            messagingTemplate.convertAndSend("/topic/comunidad-" + comunidadId, payload);

        } else if ("CHAT_VECINOS".equals(tipoNotificacion)) {
            System.out.println("DEBUG manejarMensajeChat → enviando a /topic/vecinos-" + comunidadId);
            messagingTemplate.convertAndSend("/topic/vecinos-" + comunidadId, payload);
        } else {
            System.out.println("DEBUG manejarMensajeChat → tipoNotificacion desconocido: " + tipoNotificacion);
        }

        System.out.println("DEBUG manejarMensajeChat → FIN\n");
    }
}
