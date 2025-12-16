package com.ista.springboot.web.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.ista.springboot.web.app.models.entity.MensajeComunidad;
import com.ista.springboot.web.app.models.services.IMensajeComunidadService;

@CrossOrigin(origins = { "http://localhost:4200" })
@RestController
@RequestMapping("/api")
public class MensajeComunidadRestController {

    @Autowired
    private IMensajeComunidadService mensajeService;

    @GetMapping("/mensajes-comunidad")
    public List<MensajeComunidad> index() {
        return mensajeService.findAll();
    }

    @GetMapping("/mensajes-comunidad/{id}")
    public MensajeComunidad show(@PathVariable Long id) {
        MensajeComunidad m = mensajeService.findById(id);
        if (m == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensaje de comunidad no encontrado");
        }
        return m;
    }

    @PostMapping("/mensajes-comunidad")
    @ResponseStatus(HttpStatus.CREATED)
    public MensajeComunidad create(@RequestBody MensajeComunidad mensaje) {
        return mensajeService.save(mensaje);
    }

    @PutMapping("/mensajes-comunidad/{id}")
    public MensajeComunidad update(@RequestBody MensajeComunidad mensaje, @PathVariable Long id) {
        MensajeComunidad actual = mensajeService.findById(id);
        if (actual == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensaje de comunidad no encontrado");
        }
        mensaje.setId(id);
        return mensajeService.save(mensaje);
    }

    @DeleteMapping("/mensajes-comunidad/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        MensajeComunidad m = mensajeService.findById(id);
        if (m == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensaje de comunidad no encontrado");
        }
        mensajeService.delete(id);
    }
}
