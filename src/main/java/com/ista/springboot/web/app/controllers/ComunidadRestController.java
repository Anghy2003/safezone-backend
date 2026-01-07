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
import com.ista.springboot.web.app.models.dao.IUsuario;
import com.ista.springboot.web.app.models.dao.IUsuarioComunidad;
import com.ista.springboot.web.app.models.entity.Comunidad;
import com.ista.springboot.web.app.models.entity.Usuario;
import com.ista.springboot.web.app.models.entity.UsuarioComunidad;
import com.ista.springboot.web.app.models.services.IComunidadService;
import com.ista.springboot.web.app.models.services.IUsuarioComunidadService;

@CrossOrigin(origins = { "http://localhost:5173" })
@RestController
@RequestMapping("/api")
public class ComunidadRestController {

    private static final String ADMIN_EMAIL = "safezonecomunity@gmail.com";
    private static final String ROL_ADMIN = "ADMIN";

    @Autowired
    private IComunidadService comunidadService;

    @Autowired
    private IUsuarioComunidadService usuarioComunidadService;

    // ✅ para validar rol/admin
    @Autowired
    private IUsuarioComunidad usuarioComunidadDao;

    @Autowired
    private IUsuario usuarioDao;

    // ===================== HELPERS =====================
    private void requireAdmin(Long usuarioId) {
        if (usuarioId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usuarioId requerido");
        }

        Usuario u = usuarioDao.findById(usuarioId).orElse(null);
        if (u == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }

        // ✅ 1) correo exacto del admin
        if (u.getEmail() == null || !u.getEmail().trim().equalsIgnoreCase(ADMIN_EMAIL)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el ADMIN puede realizar esta acción");
        }

        // ✅ 2) rol ADMIN en la relación usuario_comunidad (consistente con tu DTO)
        boolean esAdmin = usuarioComunidadDao.existsByUsuarioIdAndRolIgnoreCase(usuarioId, ROL_ADMIN);
        if (!esAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rol insuficiente: se requiere ADMIN");
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
        if (comunidad == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comunidad no encontrada");
        }
        return new ComunidadDTO(comunidad);
    }

    // ===================== CREAR (solo backend/admin) =====================
    // ✅ si lo vas a usar desde web/móvil, protégelo igual:
    @PostMapping("/comunidades/usuario/{usuarioId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ComunidadDTO create(@RequestBody Comunidad comunidad, @PathVariable Long usuarioId) {
        requireAdmin(usuarioId);
        Comunidad guardada = comunidadService.save(comunidad);
        return new ComunidadDTO(guardada);
    }

    // ===================== ACTUALIZAR =====================
    @PutMapping("/comunidades/{id}/usuario/{usuarioId}")
    public ComunidadDTO update(@RequestBody Comunidad comunidad, @PathVariable Long id, @PathVariable Long usuarioId) {
        requireAdmin(usuarioId);

        Comunidad actual = comunidadService.findById(id);
        if (actual == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comunidad no encontrada");
        }
        comunidad.setId(id);
        Comunidad guardada = comunidadService.save(comunidad);
        return new ComunidadDTO(guardada);
    }

    // ===================== ELIMINAR =====================
    @DeleteMapping("/comunidades/{id}/usuario/{usuarioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @PathVariable Long usuarioId) {
        requireAdmin(usuarioId);

        Comunidad comunidad = comunidadService.findById(id);
        if (comunidad == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comunidad no encontrada");
        }
        comunidadService.delete(id);
    }

    // ===================== BUSCAR POR CÓDIGO =====================
    @GetMapping("/comunidades/codigo/{codigo}")
    public ComunidadDTO buscarPorCodigo(@PathVariable String codigo) {
        Comunidad comunidad = comunidadService.findByCodigoAcceso(codigo);
        if (comunidad == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Código inválido");
        }
        return new ComunidadDTO(comunidad);
    }

    // ===================== UNIR USUARIO A COMUNIDAD =====================
    @PostMapping("/comunidades/unirse/{codigoAcceso}/usuario/{usuarioId}")
    public Map<String, Object> unirAComunidad(
            @PathVariable String codigoAcceso,
            @PathVariable Long usuarioId) {

        UsuarioComunidad uc = usuarioComunidadService.unirUsuarioAComunidad(usuarioId, codigoAcceso);

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

        if (req.getNombre() == null || req.getNombre().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre requerido");
        }
        if (req.getDireccion() == null || req.getDireccion().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dirección requerida");
        }
        if (req.getUsuarioId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usuarioId requerido");
        }
        if (req.getLat() == null || req.getLng() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lat/lng requerido");
        }
        if (req.getFotoUrl() == null || req.getFotoUrl().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fotoUrl requerido");
        }

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

    // ===================== APROBAR COMUNIDAD (ADMIN) =====================
    @PostMapping("/comunidades/{id}/aprobar/usuario/{usuarioId}")
    public ComunidadDTO aprobarComunidad(@PathVariable Long id, @PathVariable Long usuarioId) {
        requireAdmin(usuarioId);

        Comunidad aprobada = comunidadService.aprobarComunidad(id);
        return new ComunidadDTO(aprobada);
    }
}
