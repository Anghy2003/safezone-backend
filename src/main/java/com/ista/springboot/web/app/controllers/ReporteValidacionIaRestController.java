package com.ista.springboot.web.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.ista.springboot.web.app.models.entity.ReporteValidacionIa;
import com.ista.springboot.web.app.models.services.IReporteValidacionIaService;

@CrossOrigin(origins = { "http://localhost:4200" })
@RestController
@RequestMapping("/api")
public class ReporteValidacionIaRestController {

    @Autowired
    private IReporteValidacionIaService reporteService;

    @GetMapping("/reportes-ia")
    public List<ReporteValidacionIa> index() {
        return reporteService.findAll();
    }

    @GetMapping("/reportes-ia/{id}")
    public ReporteValidacionIa show(@PathVariable Long id) {
        ReporteValidacionIa r = reporteService.findById(id);
        if (r == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporte IA no encontrado");
        }
        return r;
    }

    @PostMapping("/reportes-ia")
    @ResponseStatus(HttpStatus.CREATED)
    public ReporteValidacionIa create(@RequestBody ReporteValidacionIa reporte) {
        return reporteService.save(reporte);
    }

    @PutMapping("/reportes-ia/{id}")
    public ReporteValidacionIa update(@RequestBody ReporteValidacionIa reporte, @PathVariable Long id) {
        ReporteValidacionIa actual = reporteService.findById(id);
        if (actual == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporte IA no encontrado");
        }
        reporte.setId(id);
        return reporteService.save(reporte);
    }

    @DeleteMapping("/reportes-ia/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        ReporteValidacionIa r = reporteService.findById(id);
        if (r == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporte IA no encontrado");
        }
        reporteService.delete(id);
    }
}
