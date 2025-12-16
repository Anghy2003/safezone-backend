package com.ista.springboot.web.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.ista.springboot.web.app.models.entity.ContactoEmergencia;
import com.ista.springboot.web.app.models.services.IContactoEmergenciaService;

@CrossOrigin(origins = { "http://localhost:4200" })
@RestController
@RequestMapping("/api")
public class ContactoEmergenciaRestController {

    @Autowired
    private IContactoEmergenciaService contactoService;

    @GetMapping("/contactos-emergencia")
    public List<ContactoEmergencia> index() {
        return contactoService.findAll();
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
    public ContactoEmergencia create(@RequestBody ContactoEmergencia contacto) {
        return contactoService.save(contacto);
    }

    @PutMapping("/contactos-emergencia/{id}")
    public ContactoEmergencia update(@RequestBody ContactoEmergencia contacto, @PathVariable Long id) {
        ContactoEmergencia actual = contactoService.findById(id);
        if (actual == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contacto de emergencia no encontrado");
        }
        contacto.setId(id);
        return contactoService.save(contacto);
    }

    @DeleteMapping("/contactos-emergencia/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        ContactoEmergencia c = contactoService.findById(id);
        if (c == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contacto de emergencia no encontrado");
        }
        contactoService.delete(id);
    }
}
