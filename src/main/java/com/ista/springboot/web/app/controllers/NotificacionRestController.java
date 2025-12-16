package com.ista.springboot.web.app.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.ista.springboot.web.app.dto.NotificacionCreateDTO;
import com.ista.springboot.web.app.dto.NotificacionDTO;
import com.ista.springboot.web.app.models.entity.Comunidad;
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
    private IUsuarioService usuarioService;

    @Autowired
    private IComunidadService comunidadService;

    @Autowired
    private FirebaseMessagingService firebaseMessagingService;

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

    // ===================== CREAR =====================
    @PostMapping("/notificaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public NotificacionDTO create(@RequestBody NotificacionCreateDTO dto) {

        // -------- 1) CREAR ENTIDAD --------
        Notificacion noti = new Notificacion();

        // Usuario emisor
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

        // -------- 2) GUARDAR BD --------
        Notificacion guardada = notificacionService.save(noti);
        System.out.println("DEBUG Notificación guardada id=" + guardada.getId());

        // -------- 3) ENVIAR FCM MASIVO --------
        try {
            if (comunidad != null && comunidad.getUsuarioComunidades() != null) {

                String titulo = guardada.getTitulo() != null
                        ? guardada.getTitulo()
                        : (guardada.getTipoNotificacion().equalsIgnoreCase("INCIDENTE_COMUNIDAD")
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

                for (UsuarioComunidad uc : comunidad.getUsuarioComunidades()) {

                    if (uc == null || uc.getUsuario() == null) continue;
                    Usuario u = uc.getUsuario();

                    // No enviar al que reportó
                    if (emisor != null && u.getId().equals(emisor.getId())) continue;

                    if (u.getFcmToken() == null || u.getFcmToken().isBlank()) continue;

                    System.out.println("DEBUG Enviando FCM a userId=" + u.getId());

                    try {
                        firebaseMessagingService.enviarNotificacionAToken(
                                u.getFcmToken(), titulo, cuerpo, data
                        );
                    } catch (Exception ex) {
                        System.out.println("ERROR enviando FCM userId=" + u.getId() + ": " + ex.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("ERROR FCM masivo: " + e.getMessage());
        }

        // -------- 4) RETORNAR DTO --------
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
