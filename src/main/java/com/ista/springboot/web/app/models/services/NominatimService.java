package com.ista.springboot.web.app.models.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

@Service
public class NominatimService {

    @Value("${nominatim.base-url:https://nominatim.openstreetmap.org}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Convierte coordenadas (lat, lon) a una dirección legible (reverse geocoding).
     */
    public String reverseGeocoding(double lat, double lon) {
        try {
            String url = String.format("%s/reverse?format=json&lat=%f&lon=%f&addressdetails=1",
                    baseUrl, lat, lon);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "SafeZone-App/1.0");
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    new URI(url),
                    HttpMethod.GET,
                    request,
                    String.class
            );

            return response.getBody(); // aquí podrías parsear JSON y extraer solo la dirección
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error construyendo URL de Nominatim", e);
        }
    }

    /**
     * Convierte una dirección a coordenadas (lat, lon) (forward geocoding).
     */
    public String forwardGeocoding(String direccion) {
        try {
            String encoded = UriUtils.encode(direccion, StandardCharsets.UTF_8);
            String url = String.format("%s/search?format=json&q=%s&limit=1",
                    baseUrl, encoded);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "SafeZone-App/1.0");
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    new URI(url),
                    HttpMethod.GET,
                    request,
                    String.class
            );

            return response.getBody(); // aquí igualmente podrías parsear lat/lon
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error construyendo URL de Nominatim", e);
        }
    }
}
