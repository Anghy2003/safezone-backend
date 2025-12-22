package com.ista.springboot.web.app.dto;

import java.math.BigDecimal;

public class ComunidadSolicitudDTO {
    private String nombre;
    private String direccion;
    private Long usuarioId;

    private Double lat;
    private Double lng;

    private BigDecimal radio;   // km
    private String fotoUrl;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public BigDecimal getRadio() { return radio; }
    public void setRadio(BigDecimal radio) { this.radio = radio; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
}
