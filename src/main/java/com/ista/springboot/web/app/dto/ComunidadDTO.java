package com.ista.springboot.web.app.dto;

import com.ista.springboot.web.app.models.entity.Comunidad;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class ComunidadDTO {
    private Long id;
    private String nombre;
    private String direccion;
    private String codigoAcceso;

    private String fotoUrl;

    private Double centroLat;
    private Double centroLng;

    private BigDecimal radioKm;
    private Boolean activa;
    private OffsetDateTime fechaCreacion;
    private String estado;

    private Long miembrosCount;

    // ✅ útil para panel admin (opcional)
    private Long solicitadaPorUsuarioId;

    public ComunidadDTO() {}

    public ComunidadDTO(Comunidad comunidad) {
        this.id = comunidad.getId();
        this.nombre = comunidad.getNombre();
        this.direccion = comunidad.getDireccion();
        this.codigoAcceso = comunidad.getCodigoAcceso();

        this.fotoUrl = comunidad.getFotoUrl();

        if (comunidad.getCentroGeografico() != null) {
            this.centroLat = comunidad.getCentroGeografico().getY();
            this.centroLng = comunidad.getCentroGeografico().getX();
        }

        this.radioKm = comunidad.getRadioKm();
        this.activa = comunidad.getActiva();
        this.fechaCreacion = comunidad.getFechaCreacion();
        this.estado = comunidad.getEstado() != null ? comunidad.getEstado().toString() : null;
        this.miembrosCount = comunidad.getMiembrosCount();

        this.solicitadaPorUsuarioId = comunidad.getSolicitadaPorUsuarioId();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getCodigoAcceso() { return codigoAcceso; }
    public void setCodigoAcceso(String codigoAcceso) { this.codigoAcceso = codigoAcceso; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public Double getCentroLat() { return centroLat; }
    public void setCentroLat(Double centroLat) { this.centroLat = centroLat; }

    public Double getCentroLng() { return centroLng; }
    public void setCentroLng(Double centroLng) { this.centroLng = centroLng; }

    public BigDecimal getRadioKm() { return radioKm; }
    public void setRadioKm(BigDecimal radioKm) { this.radioKm = radioKm; }

    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }

    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Long getMiembrosCount() { return miembrosCount; }
    public void setMiembrosCount(Long miembrosCount) { this.miembrosCount = miembrosCount; }

    public Long getSolicitadaPorUsuarioId() { return solicitadaPorUsuarioId; }
    public void setSolicitadaPorUsuarioId(Long solicitadaPorUsuarioId) {
        this.solicitadaPorUsuarioId = solicitadaPorUsuarioId;
    }
}
