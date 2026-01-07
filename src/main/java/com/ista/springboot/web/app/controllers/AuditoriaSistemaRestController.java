package com.ista.springboot.web.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.ista.springboot.web.app.models.entity.AuditoriaSistema;
import com.ista.springboot.web.app.models.services.IAuditoriaSistemaService;

@RestController
@RequestMapping("/api")
public class AuditoriaSistemaRestController {

    @Autowired
    private IAuditoriaSistemaService auditoriaService;

    @GetMapping("/auditorias")
    public List<AuditoriaSistema> index() {
        return auditoriaService.findAll();
    }

    @GetMapping("/auditorias/{id}")
    public AuditoriaSistema show(@PathVariable Long id) {
        AuditoriaSistema a = auditoriaService.findById(id);
        if (a == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de auditoría no encontrado");
        }
        return a;
    }

    @PostMapping("/auditorias")
    @ResponseStatus(HttpStatus.CREATED)
    public AuditoriaSistema create(@RequestBody AuditoriaSistema auditoria) {
        return auditoriaService.save(auditoria);
    }

    @PutMapping("/auditorias/{id}")
    public AuditoriaSistema update(@RequestBody AuditoriaSistema auditoria, @PathVariable Long id) {
        AuditoriaSistema actual = auditoriaService.findById(id);
        if (actual == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de auditoría no encontrado");
        }
        auditoria.setId(id);
        return auditoriaService.save(auditoria);
    }

    @DeleteMapping("/auditorias/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        AuditoriaSistema a = auditoriaService.findById(id);
        if (a == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de auditoría no encontrado");
        }
        auditoriaService.delete(id);
    }
}
