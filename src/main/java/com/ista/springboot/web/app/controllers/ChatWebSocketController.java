package com.ista.springboot.web.app.controllers;

import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import com.ista.springboot.web.app.dto.MensajeComunidadCreateDTO;
import com.ista.springboot.web.app.dto.MensajeComunidadDTO;
import com.ista.springboot.web.app.models.entity.Comunidad;
import com.ista.springboot.web.app.models.entity.MensajeComunidad;
import com.ista.springboot.web.app.models.entity.Usuario;
import com.ista.springboot.web.app.models.services.IComunidadService;
import com.ista.springboot.web.app.models.services.IMensajeComunidadService;
import com.ista.springboot.web.app.models.services.IUsuarioService;

@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private IMensajeComunidadService mensajeService;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private IComunidadService comunidadService;

    @MessageMapping("/chat/comunidad")
    public void enviarMensajeComunidad(MensajeComunidadCreateDTO dto) {
        MensajeComunidadDTO saved = manejarMensajeChat(dto, "COMUNIDAD");
        messagingTemplate.convertAndSend("/topic/comunidad-" + saved.getComunidadId(), saved);
    }

    @MessageMapping("/chat/vecinos")
    public void enviarMensajeVecinos(MensajeComunidadCreateDTO dto) {
        MensajeComunidadDTO saved = manejarMensajeChat(dto, "VECINOS");
        messagingTemplate.convertAndSend("/topic/vecinos-" + saved.getComunidadId(), saved);
    }

    public MensajeComunidadDTO manejarMensajeChat(MensajeComunidadCreateDTO dto, String canalDefault) {

        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload vacío");
        }
        if (dto.getUsuarioId() == null || dto.getComunidadId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usuarioId y comunidadId son obligatorios");
        }

        final boolean tieneTexto = dto.getMensaje() != null && !dto.getMensaje().trim().isEmpty();
        final boolean tieneImagen = dto.getImagenUrl() != null && !dto.getImagenUrl().isBlank();
        final boolean tieneVideo  = dto.getVideoUrl() != null && !dto.getVideoUrl().isBlank();
        final boolean tieneAudio  = dto.getAudioUrl() != null && !dto.getAudioUrl().isBlank();
        final boolean tieneAdjunto = tieneImagen || tieneVideo || tieneAudio;

        if (!tieneTexto && !tieneAdjunto) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El mensaje debe tener texto o adjunto (imagen/video/audio)"
            );
        }

        final Usuario usuario = usuarioService.findById(dto.getUsuarioId());
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no encontrado");
        }

        final Comunidad comunidad = comunidadService.findById(dto.getComunidadId());
        if (comunidad == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comunidad no encontrada");
        }

        // ===================== CANAL =====================
        String canal = (dto.getCanal() != null && !dto.getCanal().isBlank())
                ? dto.getCanal().trim().toUpperCase()
                : canalDefault;

        if (!"COMUNIDAD".equals(canal) && !"VECINOS".equals(canal)) {
            canal = canalDefault;
        }

        // ===================== TIPO =====================
        String tipo = (dto.getTipo() != null && !dto.getTipo().isBlank())
                ? dto.getTipo().trim().toLowerCase()
                : "texto";

        // Si viene adjunto y tipo viene vacío o "texto", lo inferimos si NO hay texto
        if (("texto".equals(tipo) || tipo.isBlank()) && !tieneTexto && tieneAdjunto) {
            if (tieneImagen) tipo = "imagen";
            else if (tieneVideo) tipo = "video";
            else if (tieneAudio) tipo = "audio";
        }

        // ===================== FILTRO SENSIBLE =====================
        final boolean contenidoSensible = Boolean.TRUE.equals(dto.getContenidoSensible());
        final String sensibilidadMotivo = clean(dto.getSensibilidadMotivo());
        final Double sensibilidadScore = dto.getSensibilidadScore(); // puede ser null

        // ===================== ARMAR ENTITY =====================
        MensajeComunidad m = new MensajeComunidad();
        m.setUsuario(usuario);
        m.setComunidad(comunidad);

        m.setMensaje(dto.getMensaje());
        m.setImagenUrl(dto.getImagenUrl());
        m.setVideoUrl(dto.getVideoUrl());
        m.setAudioUrl(dto.getAudioUrl());

        m.setCanal(canal);
        m.setTipo(tipo);

        if (dto.getReplyToId() != null) {
            MensajeComunidad parent = mensajeService.findById(dto.getReplyToId());
            if (parent == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "replyToId no existe");
            }
            if (parent.getComunidad() != null
                    && parent.getComunidad().getId() != null
                    && !parent.getComunidad().getId().equals(dto.getComunidadId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "replyToId pertenece a otra comunidad");
            }
            m.setReplyTo(parent);
        }

        m.setFechaEnvio(OffsetDateTime.now());

        // ✅ Guardar campos sensibles
        m.setContenidoSensible(contenidoSensible);
        m.setSensibilidadMotivo(sensibilidadMotivo);
        m.setSensibilidadScore(sensibilidadScore);

        MensajeComunidad guardado = mensajeService.save(m);
        return new MensajeComunidadDTO(guardado);
    }

    public void publicar(String destino, MensajeComunidadDTO payload) {
        messagingTemplate.convertAndSend(destino, payload);
    }

    private String clean(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
