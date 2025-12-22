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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.ista.springboot.web.app.dto.IncidenteDTO;
import com.ista.springboot.web.app.dto.IncidenteResponseDTO;
import com.ista.springboot.web.app.dto.RouteResponseDTO;
import com.ista.springboot.web.app.models.entity.Comunidad;
import com.ista.springboot.web.app.models.entity.Incidente;
import com.ista.springboot.web.app.models.entity.Usuario;
import com.ista.springboot.web.app.models.services.AlertaSmsService;
import com.ista.springboot.web.app.models.services.IComunidadService;
import com.ista.springboot.web.app.models.services.IIncidenteService;
import com.ista.springboot.web.app.models.services.IRutaService;
import com.ista.springboot.web.app.models.services.IUsuarioService;
import com.ista.springboot.web.app.models.services.NotificacionPushService;

@CrossOrigin(origins = { "http://localhost:4200", "http://10.0.2.2:4200", "*" })
@RestController
@RequestMapping("/api")
public class IncidenteRestController {

    @Autowired private IIncidenteService incidenteService;
    @Autowired private IUsuarioService usuarioService;
    @Autowired private IComunidadService comunidadService;
    @Autowired private IRutaService rutaService;
    @Autowired private NotificacionPushService notificacionPushService;

    // ✅ NUEVO: Envío SMS a contactos de emergencia (en background con @Async)
    @Autowired private AlertaSmsService alertaSmsService;

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

        if (dto.getTipo() == null || dto.getTipo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de incidente es obligatorio");
        }

        try {
            // =========================================================
            // 1) VALIDAR QUE EL ANÁLISIS IA EXISTA
            //    - Si no hay aiPrioridad o aiPosibleFalso => NO GUARDAR
            //    - Mensaje único: "No se realizó el análisis sin conexión."
            // =========================================================
            if (dto.getAiPrioridad() == null || dto.getAiPosibleFalso() == null) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "No se realizó el análisis sin conexión."
                );
            }

            // Normalizar valores IA
            final boolean posibleFalso = Boolean.TRUE.equals(dto.getAiPosibleFalso());
            final String aiPrioridad = normalize(dto.getAiPrioridad());

            // =========================================================
            // 2) FILTRO: SI IA MARCA POSIBLE FALSO O PRIORIDAD BAJA => NO GUARDAR
            // =========================================================
            if (posibleFalso || "BAJA".equalsIgnoreCase(aiPrioridad)) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "No se realizó el análisis sin conexión."
                );
            }

            Incidente incidente = new Incidente();

            // ================= DATOS BÁSICOS =================
            incidente.setTipo(dto.getTipo());
            incidente.setDescripcion(dto.getDescripcion());

            // Medios adjuntos
            incidente.setImagenUrl(dto.getImagenUrl());
            incidente.setVideoUrl(dto.getVideoUrl());
            incidente.setAudioUrl(dto.getAudioUrl());

            // Prioridad final (IA > manual > ALTA por defecto)
            String prioridadFinal =
                    dto.getAiPrioridad() != null ? dto.getAiPrioridad()
                    : (dto.getNivelPrioridad() != null ? dto.getNivelPrioridad()
                    : "ALTA");

            incidente.setNivelPrioridad(prioridadFinal);

            // ================= UBICACIÓN =================
            if (dto.getLat() != null && dto.getLng() != null) {
                Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(dto.getLng(), dto.getLat()));
                incidente.setUbicacion(point);
            }

            // ================= USUARIO =================
            Long usuarioId = extractUsuarioId(dto);
            Usuario usuario = null;

            if (usuarioId != null) {
                usuario = usuarioService.findById(usuarioId);
                if (usuario == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no encontrado");
                }
                incidente.setUsuario(usuario);
            }

            // ================= COMUNIDAD =================
            Long comunidadId = extractComunidadId(dto);
            Comunidad comunidad = null;

            if (comunidadId != null) {
                comunidad = comunidadService.findById(comunidadId);
                if (comunidad == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comunidad no encontrada");
                }
                incidente.setComunidad(comunidad);
            }

            // Fechas
            incidente.setFechaCreacion(OffsetDateTime.now());
            incidente.setEstado("pendiente");

            // ================= DATOS IA =================
            incidente.setAiCategoria(dto.getAiCategoria());
            incidente.setAiPrioridad(dto.getAiPrioridad());
            incidente.setAiPosibleFalso(dto.getAiPosibleFalso());
            incidente.setAiConfianza(dto.getAiConfianza());
            incidente.setAiMotivos(dto.getAiMotivos());
            incidente.setAiRiesgos(dto.getAiRiesgos());
            incidente.setAiAccionRecomendada(dto.getAiAccionRecomendada());
            incidente.setAiAnalizadoEn(OffsetDateTime.now());

            // ================= GUARDAR =================
            Incidente guardado = incidenteService.save(incidente);

            // =========================================================
            // PUSH NOTIFICATION (comunidad / vecinos)
            // =========================================================
            notificacionPushService.notificarIncidente(
                    guardado,
                    usuario,
                    resolveTipoNotificacion(dto)
            );

            // =========================================================
            // ✅ SMS A CONTACTOS DE EMERGENCIA DEL USUARIO (ASYNC)
            // =========================================================
            if (usuario != null && usuario.getId() != null) {
                alertaSmsService.enviarSmsAContactosDelUsuario(
                        usuario.getId(),
                        guardado
                );
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
        if (dto.getAudioUrl() != null) actual.setVideoUrl(dto.getVideoUrl());
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

        if ("resuelto".equalsIgnoreCase(estado)) {
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

        RouteResponseDTO dto = rutaService.route(usuarioLat, usuarioLng, incLat, incLng);
        return ResponseEntity.ok(dto);
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
}
