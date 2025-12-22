package com.ista.springboot.web.app.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ista.springboot.web.app.dto.ZonaRiesgoResponseDTO;
import com.ista.springboot.web.app.models.services.RiesgoZonaService;

@RestController
@RequestMapping("/api/riesgo")
@CrossOrigin(origins = { "http://localhost:4200", "http://10.0.2.2:4200", "*" })
public class RiesgoZonaRestController {

    private final RiesgoZonaService riesgoZonaService;

    public RiesgoZonaRestController(RiesgoZonaService riesgoZonaService) {
        this.riesgoZonaService = riesgoZonaService;
    }

    /**
     * Ejemplo:
     * GET /api/riesgo/zona?lat=-2.8974&lng=-79.0336&radioM=200&dias=30
     */
    @GetMapping("/zona")
    public ResponseEntity<ZonaRiesgoResponseDTO> zona(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "200") int radioM,
            @RequestParam(defaultValue = "30") int dias
    ) {
        return ResponseEntity.ok(riesgoZonaService.evaluar(lat, lng, radioM, dias));
    }
}
