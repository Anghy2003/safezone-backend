package com.ista.springboot.web.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.ista.springboot.web.app.models.entity.ContactoEmergencia;
import com.ista.springboot.web.app.models.entity.Usuario;
import com.ista.springboot.web.app.models.services.IContactoEmergenciaService;
import com.ista.springboot.web.app.models.services.IUsuarioService;

@CrossOrigin(origins = { "http://localhost:4200" })
@RestController
@RequestMapping("/api")
public class ContactoEmergenciaRestController {

    @Autowired
    private IContactoEmergenciaService contactoService;

    @Autowired
    private IUsuarioService usuarioService;

    // (Opcional) Admin: ver todos
    @GetMapping("/contactos-emergencia")
    public List<ContactoEmergencia> index() {
        return contactoService.findAll();
    }

    // ✅ NUEVO: SOLO los activos del usuario logueado (APP)
    @GetMapping("/contactos-emergencia/mios")
    public List<ContactoEmergencia> misContactosActivos(
            @RequestHeader("X-User-Id") Long userId
    ) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Falta X-User-Id");
        }
        return contactoService.findActivosByUsuarioId(userId);
    }

    @GetMapping("/contactos-emergencia/{id}")
    public ContactoEmergencia show(@PathVariable Long id) {
        ContactoEmergencia c = contactoService.findById(id);
        if (c == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contacto de emergencia no encontrado");
        }
        return c;
    }

    @PostMapping("/contactos-emergencia")
    @ResponseStatus(HttpStatus.CREATED)
    public ContactoEmergencia create(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ContactoEmergencia contacto
    ) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Falta X-User-Id");
        }

        Usuario u = usuarioService.findById(userId);
        if (u == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido");
        }

        // ✅ asignar dueño aquí (NO desde Flutter)
        contacto.setUsuario(u);

        // defaults
        if (contacto.getActivo() == null) contacto.setActivo(true);

        return contactoService.save(contacto);
    }

    @PutMapping("/contactos-emergencia/{id}")
    public ContactoEmergencia update(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ContactoEmergencia contacto,
            @PathVariable Long id
    ) {
        ContactoEmergencia actual = contactoService.findById(id);
        if (actual == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contacto de emergencia no encontrado");
        }

        // ✅ seguridad: solo dueño edita
        if (userId == null || actual.getUsuario() == null || !actual.getUsuario().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes editar este contacto");
        }

        // ✅ conservar usuario dueño
        contacto.setId(id);
        contacto.setUsuario(actual.getUsuario());

        return contactoService.save(contacto);
    }

    @DeleteMapping("/contactos-emergencia/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id
    ) {
        ContactoEmergencia c = contactoService.findById(id);
        if (c == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contacto de emergencia no encontrado");
        }

        // ✅ seguridad: solo dueño elimina
        if (userId == null || c.getUsuario() == null || !c.getUsuario().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes eliminar este contacto");
        }

        contactoService.delete(id);
    }
}
