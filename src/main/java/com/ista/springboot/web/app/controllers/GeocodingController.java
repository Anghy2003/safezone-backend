package com.ista.springboot.web.app.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ista.springboot.web.app.models.services.NominatimService;

@RestController
@RequestMapping("/api/geocoding")
@CrossOrigin(origins = "*")
public class GeocodingController {

    private final NominatimService nominatimService;

    public GeocodingController(NominatimService nominatimService) {
        this.nominatimService = nominatimService;
    }

    // Reverse geocoding: lat/lon -> dirección
    @GetMapping("/reverse")
    public ResponseEntity<String> reverse(
            @RequestParam double lat,
            @RequestParam double lon
    ) {
        String json = nominatimService.reverseGeocoding(lat, lon);
        return ResponseEntity.ok(json);
    }

    // Forward geocoding: dirección -> lat/lon
    @GetMapping("/search")
    public ResponseEntity<String> forward(
            @RequestParam("direccion") String direccion
    ) {
        String json = nominatimService.forwardGeocoding(direccion);
        return ResponseEntity.ok(json);
    }
}
