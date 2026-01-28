package com.ista.springboot.web.app.controllers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.ista.springboot.web.app.dto.IncidenteDTO;
import com.ista.springboot.web.app.dto.IncidenteResponseDTO;
import com.ista.springboot.web.app.dto.MensajeComunidadCreateDTO;
import com.ista.springboot.web.app.dto.MensajeComunidadDTO;
import com.ista.springboot.web.app.dto.RouteResponseDTO;
import com.ista.springboot.web.app.dto.UsuarioCercanoDTO;
import com.ista.springboot.web.app.models.entity.Comunidad;
import com.ista.springboot.web.app.models.entity.Incidente;
import com.ista.springboot.web.app.models.entity.Usuario;
import com.ista.springboot.web.app.models.services.AlertaSmsService;
import com.ista.springboot.web.app.models.services.IComunidadService;
import com.ista.springboot.web.app.models.services.IIncidenteService;
import com.ista.springboot.web.app.models.services.IRutaService;
import com.ista.springboot.web.app.models.services.IUbicacionUsuarioService;
import com.ista.springboot.web.app.models.services.IUsuarioService;
import com.ista.springboot.web.app.models.services.NotificacionPushService;
import com.ista.springboot.web.app.models.services.RateLimitService;

@CrossOrigin(origins = { "http://localhost:4200", "http://10.0.2.2:4200", "*" })
@RestController
@RequestMapping("/api")
public class IncidenteRestController {

    @Autowired private IIncidenteService incidenteService;
    @Autowired private IUsuarioService usuarioService;
    @Autowired private IComunidadService comunidadService;
    @Autowired private IRutaService rutaService;
    @Autowired private NotificacionPushService notificacionPushService;
    @Autowired private AlertaSmsService alertaSmsService;
    @Autowired private RateLimitService rateLimitService;
    
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private ChatWebSocketController chatController;
    @Autowired private IUbicacionUsuarioService ubicacionUsuarioService;

    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

    // ===================== LISTAR TODOS =====================
    @GetMapping("/incidentes")
    public List<IncidenteResponseDTO> index() {
        return incidenteService.findAll()
                .stream()
                .map(IncidenteResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ===================== OBTENER UNO =====================
    @GetMapping("/incidentes/{id}")
    public IncidenteResponseDTO show(@PathVariable Long id) {
        Incidente incidente = incidenteService.findById(id);
        if (incidente == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Incidente no encontrado");
        }
        return new IncidenteResponseDTO(incidente);
    }

    // ===================== CREAR INCIDENTE =====================
    @PostMapping("/incidentes")
    public ResponseEntity<IncidenteResponseDTO> create(@RequestBody IncidenteDTO dto) {

        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload vacío");
        }
        if (safeTrim(dto.getTipo()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de incidente es obligatorio");
        }

        try {
            // =========================================================
            // ✅ Idempotencia (CASO A): evita duplicados por reintentos
            // =========================================================
            final String clientGeneratedId = safeTrim(dto.getClientGeneratedId());
            if (clientGeneratedId != null) {
                Incidente existente = incidenteService.findByClientGeneratedId(clientGeneratedId);
                if (existente != null) {
                    // Ya existe: devolverlo sin reenviar notificaciones/SMS
                    // ✅ Importante: no consume rate-limit
                    return ResponseEntity.ok(new IncidenteResponseDTO(existente));
                }
            }

            // =========================================================
            // ✅ USUARIO (lo mantienes igual)
            // =========================================================
            Long usuarioId = extractUsuarioId(dto);
            if (usuarioId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usuarioId es obligatorio");
            }
            Usuario usuario = usuarioService.findById(usuarioId);
            if (usuario == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no encontrado");
            }

            // =========================================================
            // ✅ RATE LIMIT (usa tu RateLimitService TAL CUAL)
            // - Aquí sí cuenta porque ya sabemos que NO es duplicado
            // - y que el usuario existe
            // =========================================================
            rateLimitService.checkAndConsumeOrThrow(usuarioId);

            // =========================================================
            // ✅ DIRECTO SIN IA: tipos que empiezan con SOS_ o DIRECTO_
            // =========================================================
            final String tipoReq = dto.getTipo().trim().toUpperCase();
            final boolean esDirecto = tipoReq.startsWith("SOS_") || tipoReq.startsWith("DIRECTO_");

            // =========================================================
            // ✅ VALIDAR IA SOLO SI NO ES DIRECTO
            // =========================================================
            if (!esDirecto) {
                if (dto.getAiPrioridad() == null || dto.getAiPosibleFalso() == null) {
                    throw new ResponseStatusException(
                            HttpStatus.UNPROCESSABLE_ENTITY,
                            "Reporte marcado para revisión (validación de seguridad)"
                    );
                }

                final boolean posibleFalso = Boolean.TRUE.equals(dto.getAiPosibleFalso());
                final String aiPrioridad = normalize(dto.getAiPrioridad());

                if (posibleFalso || "BAJA".equalsIgnoreCase(aiPrioridad)) {
                    throw new ResponseStatusException(
                            HttpStatus.UNPROCESSABLE_ENTITY,
                            "Incidente bloqueado por ser (posible falso o prioridad BAJA)"
                    );
                }
            }

            Incidente incidente = new Incidente();

            // ================= DATOS BÁSICOS =================
            incidente.setTipo(dto.getTipo());
            incidente.setDescripcion(dto.getDescripcion());

            incidente.setImagenUrl(dto.getImagenUrl());
            incidente.setVideoUrl(dto.getVideoUrl());
            incidente.setAudioUrl(dto.getAudioUrl());

            // ================= PRIORIDAD FINAL =================
            String prioridadFinal;
            if (esDirecto) {
                prioridadFinal = (safeTrim(dto.getNivelPrioridad()) != null) ? dto.getNivelPrioridad() : "ALTA";
            } else {
                prioridadFinal = (safeTrim(dto.getAiPrioridad()) != null)
                        ? dto.getAiPrioridad()
                        : (safeTrim(dto.getNivelPrioridad()) != null ? dto.getNivelPrioridad() : "ALTA");
            }
            incidente.setNivelPrioridad(prioridadFinal);

            // ================= UBICACIÓN =================
            if (dto.getLat() != null && dto.getLng() != null) {
                Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(dto.getLng(), dto.getLat()));
                incidente.setUbicacion(point);
            }

            // ================= USUARIO =================
            incidente.setUsuario(usuario);

            // ================= COMUNIDAD =================
            Long comunidadId = extractComunidadId(dto);
            if (comunidadId != null) {
                Comunidad comunidad = comunidadService.findById(comunidadId);
                if (comunidad == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comunidad no encontrada");
                }
                incidente.setComunidad(comunidad);
            }

            // ================= OFFLINE / SYNC (CASO A) =================
            incidente.setClientGeneratedId(clientGeneratedId);
            incidente.setCanalEnvio(safeTrim(dto.getCanalEnvio())); // ONLINE | OFFLINE_SMS | OFFLINE_QUEUE
            incidente.setSmsEnviadoPorCliente(Boolean.TRUE.equals(dto.getSmsEnviadoPorCliente()));

            // ================= FECHAS / ESTADO =================
            incidente.setFechaCreacion(OffsetDateTime.now());
            incidente.setEstado("PENDIENTE");

            // ================= DATOS IA (solo si NO es directo) =================
            if (!esDirecto) {
                incidente.setAiCategoria(dto.getAiCategoria());
                incidente.setAiPrioridad(dto.getAiPrioridad());
                incidente.setAiPosibleFalso(dto.getAiPosibleFalso());
                incidente.setAiConfianza(dto.getAiConfianza());
                incidente.setAiMotivos(dto.getAiMotivos());
                incidente.setAiRiesgos(dto.getAiRiesgos());
                incidente.setAiAccionRecomendada(dto.getAiAccionRecomendada());
                incidente.setAiAnalizadoEn(OffsetDateTime.now());
            }

            // ================= GUARDAR =================
            Incidente guardado = incidenteService.save(incidente);

            // ================= PUSH =================
            notificacionPushService.notificarIncidente(
                    guardado,
                    usuario,
                    resolveTipoNotificacion(dto)
            );

            // ================= SMS A CONTACTOS =================
            // ✅ si el cliente ya envió SMS offline, el backend no debe duplicar
            final boolean smsYaEnviadoCliente = Boolean.TRUE.equals(dto.getSmsEnviadoPorCliente());
            if (!smsYaEnviadoCliente) {
                alertaSmsService.enviarSmsAContactosDelUsuario(usuario.getId(), guardado);
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new IncidenteResponseDTO(guardado));

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al registrar incidente: " + e.getMessage()
            );
        }
    }

    // ===================== ACTUALIZAR INCIDENTE =====================
    @PutMapping("/incidentes/{id}")
    public IncidenteResponseDTO update(@RequestBody IncidenteDTO dto, @PathVariable Long id) {

        Incidente actual = incidenteService.findById(id);
        if (actual == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Incidente no encontrado");
        }

        if (dto.getTipo() != null) actual.setTipo(dto.getTipo());
        if (dto.getDescripcion() != null) actual.setDescripcion(dto.getDescripcion());
        if (dto.getNivelPrioridad() != null) actual.setNivelPrioridad(dto.getNivelPrioridad());

        if (dto.getImagenUrl() != null) actual.setImagenUrl(dto.getImagenUrl());
        if (dto.getVideoUrl() != null) actual.setVideoUrl(dto.getVideoUrl());
        if (dto.getAudioUrl() != null) actual.setAudioUrl(dto.getAudioUrl());

        if (dto.getLat() != null && dto.getLng() != null) {
            Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(dto.getLng(), dto.getLat()));
            actual.setUbicacion(point);
        }

        Incidente guardado = incidenteService.save(actual);
        return new IncidenteResponseDTO(guardado);
    }

    // ===================== CAMBIAR ESTADO =====================
    @PutMapping("/incidentes/{id}/estado")
    public IncidenteResponseDTO cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado,
            @RequestParam(required = false) Long moderadorId) {

        Incidente incidente = incidenteService.findById(id);
        if (incidente == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Incidente no encontrado");
        }

        incidente.setEstado(estado);

        if (moderadorId != null) {
            Usuario moderador = usuarioService.findById(moderadorId);
            if (moderador != null) {
                incidente.setModeradoPor(moderador);
            }
        }

        if ("RESUELTO".equalsIgnoreCase(estado)) {
            incidente.setFechaResolucion(OffsetDateTime.now());
        }

        Incidente guardado = incidenteService.save(incidente);
        return new IncidenteResponseDTO(guardado);
    }

    // ===================== RUTA HACIA INCIDENTE =====================
    @GetMapping("/incidentes/{id}/ruta")
    public ResponseEntity<RouteResponseDTO> rutaHaciaIncidente(
            @PathVariable Long id,
            @RequestParam double usuarioLat,
            @RequestParam double usuarioLng) {

        Incidente incidente = incidenteService.findById(id);
        if (incidente == null || incidente.getUbicacion() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Incidente no encontrado o sin ubicación");
        }

        double incLat = incidente.getUbicacion().getY();
        double incLng = incidente.getUbicacion().getX();

        RouteResponseDTO dto2 = rutaService.route(usuarioLat, usuarioLng, incLat, incLng);
        return ResponseEntity.ok(dto2);
    }

    // ===================== DUAL BROADCAST (NUEVO ENDPOINT) =====================
    @PostMapping("/incidentes/broadcast-dual")
    public ResponseEntity<String> broadcastDual(@RequestBody BroadcastDualRequest request) {
        try {
            // ===== 1) BROADCAST A LA COMUNIDAD =====
            MensajeComunidadCreateDTO dtoComunidad = new MensajeComunidadCreateDTO();
            dtoComunidad.setUsuarioId(request.getUsuarioId());
            dtoComunidad.setComunidadId(request.getComunidadId());
            dtoComunidad.setCanal(request.getCanal() != null ? request.getCanal() : "COMUNIDAD");
            dtoComunidad.setTipo("incidente");
            dtoComunidad.setMensaje(request.getDescripcion());
            dtoComunidad.setImagenUrl(request.getImagenUrl());
            dtoComunidad.setVideoUrl(request.getVideoUrl());
            dtoComunidad.setAudioUrl(request.getAudioUrl());

            MensajeComunidadDTO savedComunidad = chatController.manejarMensajeChat(
                dtoComunidad, 
                request.getCanal() != null ? request.getCanal() : "COMUNIDAD"
            );

            String topicComunidad = (request.getCanal() != null && request.getCanal().equalsIgnoreCase("VECINOS"))
                ? "/topic/vecinos-" + request.getComunidadId()
                : "/topic/comunidad-" + request.getComunidadId();

            messagingTemplate.convertAndSend(topicComunidad, savedComunidad);

            // ===== 2) BROADCAST A USUARIOS CERCANOS GPS =====
            if (request.getLat() != null && request.getLng() != null) {
                double safeRadio = (request.getRadio() != null) 
                    ? Math.min(Math.max(request.getRadio(), 50.0), 5000.0) 
                    : 2000.0;

                List<UsuarioCercanoDTO> cercanos = ubicacionUsuarioService.findNearby(
                    request.getLat(),
                    request.getLng(),
                    safeRadio,
                    20,
                    500
                );

                MensajeComunidadCreateDTO dtoNearby = new MensajeComunidadCreateDTO();
                dtoNearby.setUsuarioId(request.getUsuarioId());
                dtoNearby.setComunidadId(request.getComunidadId());
                dtoNearby.setCanal("NEARBY");
                dtoNearby.setTipo("incidente");
                dtoNearby.setMensaje(request.getDescripcion());
                dtoNearby.setImagenUrl(request.getImagenUrl());
                dtoNearby.setVideoUrl(request.getVideoUrl());
                dtoNearby.setAudioUrl(request.getAudioUrl());
                dtoNearby.setLat(request.getLat());
                dtoNearby.setLng(request.getLng());
                dtoNearby.setRadio(safeRadio);

                MensajeComunidadDTO savedNearby = chatController.manejarMensajeChat(dtoNearby, "NEARBY");

                for (UsuarioCercanoDTO u : cercanos) {
                    if (u == null || u.getId() == null) continue;
                    Long receptorId = u.getId();
                    if (receptorId.equals(request.getUsuarioId())) continue;

                    messagingTemplate.convertAndSendToUser(
                        receptorId.toString(),
                        "/queue/nearby",
                        savedNearby
                    );
                }
            }

            return ResponseEntity.ok("Broadcast dual completado");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // ===================== HELPERS =====================
    private Long extractUsuarioId(IncidenteDTO dto) {
        if (dto.getUsuarioId() != null) return dto.getUsuarioId();
        if (dto.getUsuario() != null && dto.getUsuario().getId() != null) return dto.getUsuario().getId();
        return null;
    }

    private Long extractComunidadId(IncidenteDTO dto) {
        if (dto.getComunidadId() != null) return dto.getComunidadId();
        if (dto.getComunidad() != null && dto.getComunidad().getId() != null) return dto.getComunidad().getId();
        return null;
    }

    private String resolveTipoNotificacion(IncidenteDTO dto) {
        String tipo = dto.getTipo() != null ? dto.getTipo().toUpperCase() : "";
        return tipo.contains("VECINOS") ? "INCIDENTE_VECINOS" : "INCIDENTE_COMUNIDAD";
    }

    private static String normalize(String s) {
        return (s == null) ? null : s.trim().toUpperCase();
    }

    private static String safeTrim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    // ===== DTO REQUEST =====
    public static class BroadcastDualRequest {
        private Long usuarioId;
        private Long comunidadId;
        private String canal;
        private String descripcion;
        private String imagenUrl;
        private String videoUrl;
        private String audioUrl;
        private Double lat;
        private Double lng;
        private Double radio;

        public Long getUsuarioId() { return usuarioId; }
        public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

        public Long getComunidadId() { return comunidadId; }
        public void setComunidadId(Long comunidadId) { this.comunidadId = comunidadId; }

        public String getCanal() { return canal; }
        public void setCanal(String canal) { this.canal = canal; }

        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

        public String getImagenUrl() { return imagenUrl; }
        public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

        public String getVideoUrl() { return videoUrl; }
        public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

        public String getAudioUrl() { return audioUrl; }
        public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }

        public Double getLng() { return lng; }
        public void setLng(Double lng) { this.lng = lng; }

        public Double getRadio() { return radio; }
        public void setRadio(Double radio) { this.radio = radio; }
    }
}