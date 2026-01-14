package com.ista.springboot.web.app.controllers;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import com.ista.springboot.web.app.dto.MensajeComunidadCreateDTO;
import com.ista.springboot.web.app.dto.MensajeComunidadDTO;
import com.ista.springboot.web.app.models.dao.IUsuarioComunidad;
import com.ista.springboot.web.app.models.entity.Comunidad;
import com.ista.springboot.web.app.models.entity.MensajeComunidad;
import com.ista.springboot.web.app.models.entity.Notificacion;
import com.ista.springboot.web.app.models.entity.Usuario;
import com.ista.springboot.web.app.models.entity.UsuarioComunidad;
import com.ista.springboot.web.app.models.services.IComunidadService;
import com.ista.springboot.web.app.models.services.IMensajeComunidadService;
import com.ista.springboot.web.app.models.services.INotificacionService;
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

    @Autowired
    private IUsuarioComunidad usuarioComunidadDao;

    @Autowired
    private INotificacionService notificacionService;

    private static final String ESTADO_ACTIVO = "activo";

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

        final boolean tieneTexto  = dto.getMensaje() != null && !dto.getMensaje().trim().isEmpty();
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

        if (("texto".equals(tipo) || tipo.isBlank()) && !tieneTexto && tieneAdjunto) {
            if (tieneImagen) tipo = "imagen";
            else if (tieneVideo) tipo = "video";
            else if (tieneAudio) tipo = "audio";
        }

        // ===================== FILTRO SENSIBLE (tu lógica) =====================
        final boolean contenidoSensible = tieneAdjunto;
        final String sensibilidadMotivo = contenidoSensible ? "Reporte / Incidente" : null;
        final Double sensibilidadScore  = contenidoSensible ? 1.0 : null;

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

        m.setContenidoSensible(contenidoSensible);
        m.setSensibilidadMotivo(sensibilidadMotivo);
        m.setSensibilidadScore(sensibilidadScore);

        MensajeComunidad guardado = mensajeService.save(m);

        // ✅ Crear notificaciones por usuario para puntito en Mis comunidades
        try {
            crearNotificacionesChat(usuario, comunidad, canal, tipo, tieneTexto, dto.getMensaje());
        } catch (Exception ex) {
            System.out.println("WARN no se pudo crear notificaciones chat: " + ex.getMessage());
        }

        return new MensajeComunidadDTO(guardado);
    }

    private void crearNotificacionesChat(
            Usuario emisor,
            Comunidad comunidad,
            String canal,
            String tipo,
            boolean tieneTexto,
            String texto
    ) {
        if (comunidad == null || comunidad.getId() == null) return;

        List<UsuarioComunidad> miembrosActivos =
                usuarioComunidadDao.findByComunidadIdAndEstadoIgnoreCase(comunidad.getId(), ESTADO_ACTIVO);

        final String tipoNoti = "CHAT_" + canal; // CHAT_COMUNIDAD / CHAT_VECINOS
        final String titulo = "Nuevo mensaje en " + (comunidad.getNombre() != null ? comunidad.getNombre() : "tu comunidad");

        final String cuerpo = tieneTexto
                ? (texto != null ? texto.trim() : "Nuevo mensaje")
                : ("Nuevo " + (tipo != null ? tipo : "mensaje"));

        for (UsuarioComunidad uc : miembrosActivos) {
            if (uc == null || uc.getUsuario() == null) continue;

            Usuario receptor = uc.getUsuario();
            if (receptor.getId() == null) continue;

            if (emisor != null && emisor.getId() != null && receptor.getId().equals(emisor.getId())) continue;

            Notificacion n = new Notificacion();
            n.setUsuario(receptor); // ✅ receptor
            n.setComunidad(comunidad);
            n.setTipoNotificacion(tipoNoti);
            n.setTitulo(titulo);
            n.setMensaje(cuerpo);

            n.setLeido(false);
            n.setFechaEnvio(OffsetDateTime.now());

            n.setTieneFoto("imagen".equalsIgnoreCase(tipo));
            n.setTieneVideo("video".equalsIgnoreCase(tipo));
            n.setTieneAudio("audio".equalsIgnoreCase(tipo));

            notificacionService.save(n);
        }
    }

    public void publicar(String destino, MensajeComunidadDTO payload) {
        messagingTemplate.convertAndSend(destino, payload);
    }
}
