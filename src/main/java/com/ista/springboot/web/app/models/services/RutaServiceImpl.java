package com.ista.springboot.web.app.models.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ista.springboot.web.app.dto.RouteResponseDTO;

@Service
public class RutaServiceImpl implements IRutaService {

    // Puedes apuntar a tu propio OSRM si lo hosteas
    private static final String OSRM_BASE = "https://router.project-osrm.org";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @SuppressWarnings("unchecked")
    public RouteResponseDTO route(double fromLat, double fromLng, double toLat, double toLng) {

        // OSRM usa orden lng,lat
        String url = OSRM_BASE + "/route/v1/driving/"
                + fromLng + "," + fromLat + ";"
                + toLng + "," + toLat
                + "?overview=full&geometries=geojson&steps=false";

        ResponseEntity<Map> resp = restTemplate.getForEntity(url, Map.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new RuntimeException("OSRM no respondió correctamente");
        }

        Map<String, Object> body = resp.getBody();
        List<Map<String, Object>> routes = (List<Map<String, Object>>) body.get("routes");

        if (routes == null || routes.isEmpty()) {
            throw new RuntimeException("OSRM no devolvió rutas");
        }

        Map<String, Object> firstRoute = routes.get(0);

        Double distance = toDouble(firstRoute.get("distance"));
        Double duration = toDouble(firstRoute.get("duration"));

        // geometry: { "coordinates": [ [lng,lat], [lng,lat], ... ] }
        Map<String, Object> geometry = (Map<String, Object>) firstRoute.get("geometry");
        List<List<Number>> coords = (List<List<Number>>) geometry.get("coordinates");

        // Convertir las coordenadas de la ruta en una lista de puntos
        List<List<Double>> coordinates = new ArrayList<>();
        if (coords != null) {
            for (List<Number> c : coords) {
                // c[0]=lng, c[1]=lat
                coordinates.add(List.of(c.get(0).doubleValue(), c.get(1).doubleValue()));
            }
        }

        // Retornar los datos de la ruta
        return new RouteResponseDTO(true, distance, duration, coordinates);
    }

    private Double toDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.doubleValue();
        return Double.valueOf(v.toString());
    }
}
