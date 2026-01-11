package com.ista.springboot.web.app.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.ista.springboot.web.app.dto.ComunidadDTO;
import com.ista.springboot.web.app.dto.ComunidadSolicitudDTO;
import com.ista.springboot.web.app.dto.SolicitudJoinDTO; // ✅ Opción B (DTO)
import com.ista.springboot.web.app.models.dao.IUsuario;
import com.ista.springboot.web.app.models.dao.IUsuarioComunidad;
import com.ista.springboot.web.app.models.entity.Comunidad;
import com.ista.springboot.web.app.models.entity.ComunidadInvitacion;
import com.ista.springboot.web.app.models.entity.Usuario;
import com.ista.springboot.web.app.models.entity.UsuarioComunidad;
import com.ista.springboot.web.app.models.services.ComunidadInvitacionService;
import com.ista.springboot.web.app.models.services.IComunidadService;
import com.ista.springboot.web.app.models.services.IUsuarioComunidadService;

@CrossOrigin(origins = { "http://localhost:5173" })
@RestController
@RequestMapping("/api")
public class ComunidadRestController {

    private static final String SUPER_ADMIN_EMAIL = "safezonecomunity@gmail.com";

    @Autowired private IComunidadService comunidadService;
    @Autowired private IUsuarioComunidadService usuarioComunidadService;
    @Autowired private ComunidadInvitacionService invitacionService;

    @Autowired private IUsuarioComunidad usuarioComunidadDao;
    @Autowired private IUsuario usuarioDao;

    // ===================== HELPERS =====================
    private void requireSuperAdmin(Long usuarioId) {
        if (usuarioId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usuarioId requerido");

        Usuario u = usuarioDao.findById(usuarioId).orElse(null);
        if (u == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");

        if (u.getEmail() == null || !u.getEmail().trim().equalsIgnoreCase(SUPER_ADMIN_EMAIL)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el SUPER ADMIN puede realizar esta acción");
        }
    }

    // ===================== LISTAR TODAS =====================
    @GetMapping("/comunidades")
    public List<ComunidadDTO> index() {
        List<Comunidad> comunidades = comunidadService.findAll();
        return comunidades.stream().map(ComunidadDTO::new).collect(Collectors.toList());
    }

    // ===================== OBTENER UNA =====================
    @GetMapping("/comunidades/{id}")
    public ComunidadDTO show(@PathVariable Long id) {
        Comunidad comunidad = comunidadService.findById(id);
        if (comunidad == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comunidad no encontrada");
        return new ComunidadDTO(comunidad);
    }

    // ===================== CREAR (solo superadmin) =====================
    @PostMapping("/comunidades/usuario/{usuarioId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ComunidadDTO create(@RequestBody Comunidad comunidad, @PathVariable Long usuarioId) {
        requireSuperAdmin(usuarioId);
        Comunidad guardada = comunidadService.save(comunidad);
        return new ComunidadDTO(guardada);
    }

    // ===================== ACTUALIZAR =====================
    @PutMapping("/comunidades/{id}/usuario/{usuarioId}")
    public ComunidadDTO update(@RequestBody Comunidad comunidad, @PathVariable Long id, @PathVariable Long usuarioId) {
        requireSuperAdmin(usuarioId);

        Comunidad actual = comunidadService.findById(id);
        if (actual == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comunidad no encontrada");

        comunidad.setId(id);
        Comunidad guardada = comunidadService.save(comunidad);
        return new ComunidadDTO(guardada);
    }

    // ===================== ELIMINAR =====================
    @DeleteMapping("/comunidades/{id}/usuario/{usuarioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @PathVariable Long usuarioId) {
        requireSuperAdmin(usuarioId);

        Comunidad comunidad = comunidadService.findById(id);
        if (comunidad == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comunidad no encontrada");

        comunidadService.delete(id);
    }

    // ===================== BUSCAR POR CÓDIGO =====================
    @GetMapping("/comunidades/codigo/{codigo}")
    public ComunidadDTO buscarPorCodigo(@PathVariable String codigo) {
        Comunidad comunidad = comunidadService.findByCodigoAcceso(codigo);
        if (comunidad == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Código inválido");
        return new ComunidadDTO(comunidad);
    }

    // ===================== UNIR POR CÓDIGO (LEGACY) =====================
    @PostMapping("/comunidades/unirse/{codigoAcceso}/usuario/{usuarioId}")
    public Map<String, Object> unirAComunidad(@PathVariable String codigoAcceso, @PathVariable Long usuarioId) {

        UsuarioComunidad uc = usuarioComunidadService.unirUsuarioAComunidad(usuarioId, codigoAcceso);

        return Map.of(
            "success", true,
            "userId", uc.getUsuario().getId(),
            "communityId", uc.getComunidad().getId(),
            "rol", uc.getRol(),
            "estado", uc.getEstado()
        );
    }

    // ===================== UNIR POR TOKEN =====================
    @PostMapping("/comunidades/unirse-token")
    public Map<String, Object> unirPorToken(@RequestBody Map<String, Object> body) {
        Long usuarioId = body.get("usuarioId") == null ? null : Long.valueOf(body.get("usuarioId").toString());
        String token = body.get("token") == null ? null : body.get("token").toString();

        UsuarioComunidad uc = usuarioComunidadService.unirUsuarioPorToken(usuarioId, token);

        return Map.of(
            "success", true,
            "userId", uc.getUsuario().getId(),
            "communityId", uc.getComunidad().getId(),
            "rol", uc.getRol(),
            "estado", uc.getEstado()
        );
    }

    // ===================== SOLICITAR CREAR COMUNIDAD =====================
    @PostMapping("/comunidades/solicitar")
    @ResponseStatus(HttpStatus.CREATED)
    public ComunidadDTO solicitarComunidad(@RequestBody ComunidadSolicitudDTO req) {

        if (req.getNombre() == null || req.getNombre().trim().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre requerido");
        if (req.getDireccion() == null || req.getDireccion().trim().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dirección requerida");
        if (req.getUsuarioId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usuarioId requerido");
        if (req.getLat() == null || req.getLng() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lat/lng requerido");
        if (req.getFotoUrl() == null || req.getFotoUrl().trim().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fotoUrl requerido");

        Comunidad comunidad = new Comunidad();
        comunidad.setNombre(req.getNombre().trim());
        comunidad.setDireccion(req.getDireccion().trim());
        comunidad.setFotoUrl(req.getFotoUrl().trim());

        BigDecimal radio = (req.getRadio() != null) ? req.getRadio() : BigDecimal.valueOf(5.00);
        comunidad.setRadioKm(radio);

        GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
        Point p = gf.createPoint(new Coordinate(req.getLng(), req.getLat()));
        p.setSRID(4326);
        comunidad.setCentroGeografico(p);

        Comunidad guardada = comunidadService.solicitarComunidad(comunidad, req.getUsuarioId());
        return new ComunidadDTO(guardada);
    }

    // ===================== APROBAR COMUNIDAD (SUPERADMIN) =====================
    @PostMapping("/comunidades/{id}/aprobar/usuario/{usuarioId}")
    public ComunidadDTO aprobarComunidad(@PathVariable Long id, @PathVariable Long usuarioId) {
        requireSuperAdmin(usuarioId);
        Comunidad aprobada = comunidadService.aprobarComunidad(id);
        return new ComunidadDTO(aprobada);
    }

    // ===================== ADMIN: GENERAR INVITACIÓN DIRECTA (si la mantienes) =====================
    @PostMapping("/comunidades/{comunidadId}/invitaciones/usuario/{adminId}")
    public Map<String, Object> generarInvitacionParaUsuario(
            @PathVariable Long comunidadId,
            @PathVariable Long adminId,
            @RequestBody Map<String, Object> body
    ) {
        Long usuarioId = body.get("usuarioId") == null ? null : Long.valueOf(body.get("usuarioId").toString());
        Integer horasExpira = body.get("horasExpira") == null ? 24 : Integer.valueOf(body.get("horasExpira").toString());

        if (usuarioId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usuarioId requerido");

        usuarioComunidadService.requireAdminComunidad(adminId, comunidadId);

        Comunidad comunidad = comunidadService.findById(comunidadId);
        if (comunidad == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comunidad no encontrada");

        Usuario usuario = usuarioDao.findById(usuarioId).orElse(null);
        if (usuario == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");

        Usuario admin = usuarioDao.findById(adminId).orElse(null);
        if (admin == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin no encontrado");

        ComunidadInvitacion inv = invitacionService.crearInvitacion1Uso(comunidad, usuario, admin, horasExpira);

        return Map.of(
            "success", true,
            "comunidadId", comunidadId,
            "usuarioId", usuarioId,
            "token", inv.getToken(),
            "expiresAt", inv.getExpiresAt() == null ? null : inv.getExpiresAt().toString(),
            "estado", inv.getEstado()
        );
    }

    // =====================================================================
    // ===================== NUEVO FLUJO: SOLICITAR / APROBAR =====================
    // =====================================================================

    // Usuario solicita unirse a comunidad (queda pendiente)
    @PostMapping("/comunidades/{comunidadId}/solicitar-unirse/usuario/{usuarioId}")
    public Map<String, Object> solicitarUnirse(@PathVariable Long comunidadId, @PathVariable Long usuarioId) {
        UsuarioComunidad uc = usuarioComunidadService.solicitarUnirse(usuarioId, comunidadId);
        return Map.of(
            "success", true,
            "comunidadId", comunidadId,
            "usuarioId", usuarioId,
            "estado", uc.getEstado(),
            "rol", uc.getRol()
        );
    }

    // ✅ Opción B: Admin lista solicitudes pendientes pero devolviendo DTO (no entidad JPA)
    @GetMapping("/comunidades/{comunidadId}/solicitudes/usuario/{adminId}")
    public List<SolicitudJoinDTO> listarSolicitudesPendientes(
            @PathVariable Long comunidadId,
            @PathVariable Long adminId
    ) {
        return usuarioComunidadService.listarSolicitudesPendientes(adminId, comunidadId)
                .stream()
                .map(SolicitudJoinDTO::new)
                .collect(Collectors.toList());
    }

    // Admin aprueba solicitud -> genera token 1-uso
    @PostMapping("/comunidades/{comunidadId}/solicitudes/{usuarioId}/aprobar/usuario/{adminId}")
    public Map<String, Object> aprobarSolicitud(
            @PathVariable Long comunidadId,
            @PathVariable Long usuarioId,
            @PathVariable Long adminId,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        Integer horasExpira = 24;
        if (body != null && body.get("horasExpira") != null) {
            horasExpira = Integer.valueOf(body.get("horasExpira").toString());
        }

        return usuarioComunidadService.aprobarSolicitudYGenerarToken(adminId, comunidadId, usuarioId, horasExpira);
    }

    // Admin rechaza solicitud
    @PostMapping("/comunidades/{comunidadId}/solicitudes/{usuarioId}/rechazar/usuario/{adminId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rechazarSolicitud(@PathVariable Long comunidadId, @PathVariable Long usuarioId, @PathVariable Long adminId) {
        usuarioComunidadService.rechazarSolicitud(adminId, comunidadId, usuarioId);
    }
    
    @GetMapping("/usuarios/{usuarioId}/comunidades")
    public List<Map<String, Object>> misComunidades(@PathVariable Long usuarioId) {

        List<UsuarioComunidad> list = usuarioComunidadDao.findByUsuarioId(usuarioId);

        return list.stream().map(uc -> {
            Comunidad c = uc.getComunidad();
            if (c == null || c.getId() == null) {
                return Map.<String, Object>of(
                    "estado", uc.getEstado(),
                    "rol", uc.getRol(),
                    "comunidad", Map.of()
                );
            }

            long miembrosCount = usuarioComunidadDao.countByComunidadId(c.getId());

            return Map.<String, Object>of(
                "estado", uc.getEstado(),
                "rol", uc.getRol(),
                "comunidad", Map.of(
                    "id", c.getId(),
                    "nombre", c.getNombre(),
                    "fotoUrl", c.getFotoUrl(),
                    "miembrosCount", miembrosCount
                )
            );
        }).toList();
    }


}
