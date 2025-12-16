package com.ista.springboot.web.app.dto;

import java.util.List;

public class RouteResponseDTO {

    private boolean ok;
    private Double distanceMeters;
    private Double durationSeconds;

    // OSRM geojson coordinates: [[lng,lat], [lng,lat], ...]
    private List<List<Double>> coordinates;

    public RouteResponseDTO() {}

    public RouteResponseDTO(boolean ok, Double distanceMeters, Double durationSeconds, List<List<Double>> coordinates) {
        this.ok = ok;
        this.distanceMeters = distanceMeters;
        this.durationSeconds = durationSeconds;
        this.coordinates = coordinates;
    }

    public boolean isOk() { return ok; }
    public void setOk(boolean ok) { this.ok = ok; }

    public Double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(Double distanceMeters) { this.distanceMeters = distanceMeters; }

    public Double getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Double durationSeconds) { this.durationSeconds = durationSeconds; }

    public List<List<Double>> getCoordinates() { return coordinates; }
    public void setCoordinates(List<List<Double>> coordinates) { this.coordinates = coordinates; }
}
