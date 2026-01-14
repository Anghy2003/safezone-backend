package com.ista.springboot.web.app.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.ista.springboot.web.app.dto.NotificacionCreateDTO;
import com.ista.springboot.web.app.dto.NotificacionDTO;
import com.ista.springboot.web.app.models.dao.INotificacion;
import com.ista.springboot.web.app.models.dao.IUsuarioComunidad;
import com.ista.springboot.web.app.models.entity.Comunidad;
import com.ista.springboot.web.app.models.entity.EstadoComunidad;
import com.ista.springboot.web.app.models.entity.Notificacion;
import com.ista.springboot.web.app.models.entity.Usuario;
import com.ista.springboot.web.app.models.entity.UsuarioComunidad;
import com.ista.springboot.web.app.models.services.IComunidadService;
import com.ista.springboot.web.app.models.services.INotificacionService;
import com.ista.springboot.web.app.models.services.IUsuarioService;
import com.ista.springboot.web.app.models.services.FirebaseMessagingService;

@CrossOrigin(origins = {"*"})
@RestController
@RequestMapping("/api")
public class NotificacionRestController {

    @Autowired
    private INotificacionService notificacionService;

    @Autowired
    private INotificacion notificacionDao; // ✅ unread + mark-read

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private IComunidadService comunidadService;

    @Autowired
    private FirebaseMessagingService firebaseMessagingService;

    @Autowired
    private IUsuarioComunidad usuarioComunidadDao;

    private static final String ESTADO_ACTIVO = "activo";

    // ===================== LISTAR =====================
    @GetMapping("/notificaciones")
    public List<NotificacionDTO> index() {
        List<Notificacion> lista = notificacionService.findAll();
        return lista.stream().map(NotificacionDTO::new).collect(Collectors.toList());
    }

    // ===================== OBTENER =====================
    @GetMapping("/notificaciones/{id}")
    public NotificacionDTO show(@PathVariable Long id) {
        Notificacion n = notificacionService.findById(id);
        if (n == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificación no encontrada");
        }
        return new NotificacionDTO(n);
    }

    // ===================== NUEVO: UNREAD POR COMUNIDAD =====================
    // GET /api/notificaciones/unread-by-comunidad/123
    // Response: { "1": 3, "2": 1 }
    @GetMapping("/notificaciones/unread-by-comunidad/{userId}")
    public Map<Long, Long> unreadByComunidad(@PathVariable Long userId) {
        List<Object[]> rows = notificacionDao.countUnreadByComunidad(userId);

        Map<Long, Long> out = new HashMap<>();
        for (Object[] r : rows) {
            Long comunidadId = (Long) r[0];
            Long count = (Long) r[1];
            out.put(comunidadId, count);
        }
        return out;
    }

    // ===================== NUEVO: MARCAR LEÍDO POR COMUNIDAD =====================
    // POST /api/notificaciones/mark-read?userId=123&comunidadId=5
    @Transactional
    @PostMapping("/notificaciones/mark-read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@RequestParam Long userId, @RequestParam Long comunidadId) {
        notificacionDao.markReadByComunidad(userId, comunidadId);
    }

    // ===================== CREAR =====================
    @PostMapping("/notificaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public NotificacionDTO create(@RequestBody NotificacionCreateDTO dto) {

        Notificacion noti = new Notificacion();

        // Usuario emisor (según tu modelo actual)
        Usuario emisor = null;
        if (dto.getUsuarioId() != null) {
            emisor = usuarioService.findById(dto.getUsuarioId());
            if (emisor == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no encontrado");
            }
            noti.setUsuario(emisor);
        }

        // Comunidad
        Comunidad comunidad = null;
        if (dto.getComunidadId() != null) {
            comunidad = comunidadService.findById(dto.getComunidadId());
            if (comunidad == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comunidad no encontrada");
            }
            noti.setComunidad(comunidad);
        }

        noti.setTipoNotificacion(dto.getTipoNotificacion());
        noti.setTitulo(dto.getTitulo());
        noti.setMensaje(dto.getMensaje());

        noti.setTieneFoto(dto.getTieneFoto());
        noti.setTieneVideo(dto.getTieneVideo());
        noti.setTieneAudio(dto.getTieneAudio());
        noti.setTieneUbicacion(dto.getTieneUbicacion());
        noti.setLatitud(dto.getLatitud());
        noti.setLongitud(dto.getLongitud());
        noti.setDireccion(dto.getDireccion());

        Notificacion guardada = notificacionService.save(noti);
        System.out.println("DEBUG Notificación guardada id=" + guardada.getId());

        // -------- FCM masivo (tu lógica) --------
        try {
            if (comunidad != null && comunidad.getId() != null) {

                if (comunidad.getEstado() == EstadoComunidad.SUSPENDIDA) {
                    System.out.println("DEBUG Comunidad suspendida, no se envía FCM. comunidadId=" + comunidad.getId());
                    return new NotificacionDTO(guardada);
                }

                String titulo = guardada.getTitulo() != null
                        ? guardada.getTitulo()
                        : ("INCIDENTE_COMUNIDAD".equalsIgnoreCase(guardada.getTipoNotificacion())
                                ? "Incidente en tu comunidad"
                                : "Emergencia cercana");

                String cuerpo = guardada.getMensaje() != null
                        ? guardada.getMensaje()
                        : "Nueva alerta registrada";

                Map<String, String> data = new HashMap<>();
                data.put("tipoNotificacion", guardada.getTipoNotificacion());
                if (guardada.getIncidente() != null && guardada.getIncidente().getId() != null) {
                    data.put("incidenteId", guardada.getIncidente().getId().toString());
                }
                data.put("comunidadId", comunidad.getId().toString());

                List<UsuarioComunidad> miembrosActivos =
                        usuarioComunidadDao.findByComunidadIdAndEstadoIgnoreCase(comunidad.getId(), ESTADO_ACTIVO);

                for (UsuarioComunidad uc : miembrosActivos) {
                    if (uc == null || uc.getUsuario() == null) continue;

                    Usuario u = uc.getUsuario();

                    if (emisor != null && u.getId() != null && u.getId().equals(emisor.getId())) continue;
                    if (u.getFcmToken() == null || u.getFcmToken().isBlank()) continue;

                    try {
                        firebaseMessagingService.enviarNotificacionAToken(u.getFcmToken(), titulo, cuerpo, data);
                    } catch (Exception ex) {
                        System.out.println("ERROR enviando FCM userId=" + u.getId() + ": " + ex.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("ERROR FCM masivo: " + e.getMessage());
        }

        return new NotificacionDTO(guardada);
    }

    // ===================== ACTUALIZAR =====================
    @PutMapping("/notificaciones/{id}")
    public NotificacionDTO update(@RequestBody Notificacion notificacion, @PathVariable Long id) {
        Notificacion actual = notificacionService.findById(id);
        if (actual == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificación no encontrada");
        }
        notificacion.setId(id);
        return new NotificacionDTO(notificacionService.save(notificacion));
    }

    // ===================== ELIMINAR =====================
    @DeleteMapping("/notificaciones/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Notificacion n = notificacionService.findById(id);
        if (n == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificación no encontrada");
        }
        notificacionService.delete(id);
    }
}
