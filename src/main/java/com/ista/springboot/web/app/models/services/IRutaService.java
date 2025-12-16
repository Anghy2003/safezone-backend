package com.ista.springboot.web.app.models.services;

import com.ista.springboot.web.app.dto.RouteResponseDTO;

public interface IRutaService {
    RouteResponseDTO route(double fromLat, double fromLng, double toLat, double toLng);
}
