package com.ista.springboot.web.app.controllers;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.ista.springboot.web.app.dto.UsuarioCercanoDTO;
import com.ista.springboot.web.app.models.entity.UbicacionUsuario;
import com.ista.springboot.web.app.models.entity.Usuario;
import com.ista.springboot.web.app.models.services.IUbicacionUsuarioService;

@CrossOrigin(origins = { "http://localhost:4200", "*" })
@RestController
@RequestMapping("/api")
public class UbicacionUsuarioRestController {

    @Autowired
    private IUbicacionUsuarioService ubicacionUsuarioService;

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

    // ========== CRUD BÁSICO ==========

    @GetMapping("/ubicaciones-usuario")
    public List<UbicacionUsuario> index() {
        return ubicacionUsuarioService.findAll();
    }

    @GetMapping("/ubicaciones-usuario/{id}")
    public UbicacionUsuario show(@PathVariable Long id) {
        UbicacionUsuario u = ubicacionUsuarioService.findById(id);
        if (u == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ubicación de usuario no encontrada");
        }
        return u;
    }

    @PostMapping("/ubicaciones-usuario")
    @ResponseStatus(HttpStatus.CREATED)
    public UbicacionUsuario create(@RequestBody UbicacionUsuario ubicacionUsuario) {
        if (ubicacionUsuario.getUltimaActualizacion() == null) {
            ubicacionUsuario.setUltimaActualizacion(OffsetDateTime.now());
        }
        return ubicacionUsuarioService.save(ubicacionUsuario);
    }

    @PutMapping("/ubicaciones-usuario/{id}")
    public UbicacionUsuario update(@RequestBody UbicacionUsuario ubicacionUsuario, @PathVariable Long id) {
        UbicacionUsuario actual = ubicacionUsuarioService.findById(id);
        if (actual == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ubicación de usuario no encontrada");
        }

        ubicacionUsuario.setId(id);

        if (ubicacionUsuario.getUltimaActualizacion() == null) {
            ubicacionUsuario.setUltimaActualizacion(OffsetDateTime.now());
        }

        return ubicacionUsuarioService.save(ubicacionUsuario);
    }

    @DeleteMapping("/ubicaciones-usuario/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        UbicacionUsuario u = ubicacionUsuarioService.findById(id);
        if (u == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ubicación de usuario no encontrada");
        }
        ubicacionUsuarioService.delete(id);
    }

    // ========== ACTUALIZAR UBICACIÓN ACTUAL ==========

    @PostMapping("/ubicaciones-usuario/actual")
    public ResponseEntity<Map<String, Object>> actualizarUbicacionActual(
            @RequestParam Long usuarioId,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(required = false) Integer precision
    ) {
        UbicacionUsuario ubicacion = ubicacionUsuarioService.findByUsuarioId(usuarioId);

        if (ubicacion == null) {
            ubicacion = new UbicacionUsuario();
            Usuario u = new Usuario();
            u.setId(usuarioId);
            ubicacion.setUsuario(u);
        }

        Point punto = geometryFactory.createPoint(new org.locationtech.jts.geom.Coordinate(lng, lat));
        ubicacion.setUbicacion(punto);
        ubicacion.setUltimaActualizacion(OffsetDateTime.now());
        ubicacion.setPrecisionMetros(precision);

        UbicacionUsuario guardada = ubicacionUsuarioService.save(ubicacion);

        Map<String, Object> resp = new HashMap<>();
        resp.put("ok", true);
        resp.put("usuarioId", usuarioId);
        resp.put("lat", lat);
        resp.put("lng", lng);
        resp.put("precision", precision);
        resp.put("ultimaActualizacion", guardada.getUltimaActualizacion());

        return ResponseEntity.ok(resp);
    }

    // ========== USUARIOS CERCANOS (OPTIMIZADO PostGIS) ==========

    @GetMapping("/usuarios-cercanos")
    public List<UsuarioCercanoDTO> usuariosCercanos(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "500") double radio,
            @RequestParam(defaultValue = "20") int lastMinutes,
            @RequestParam(defaultValue = "200") int limit
    ) {
        double safeRadio = Math.min(Math.max(radio, 10), 5000);
        int safeLimit = Math.min(Math.max(limit, 1), 500);
        int safeMinutes = Math.min(Math.max(lastMinutes, 1), 120);

        return ubicacionUsuarioService.findNearby(lat, lng, safeRadio, safeMinutes, safeLimit);
    }
}
