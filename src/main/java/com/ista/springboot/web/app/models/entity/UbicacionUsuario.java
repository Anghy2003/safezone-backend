package com.ista.springboot.web.app.models.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;

import org.locationtech.jts.geom.Point;

import jakarta.persistence.*;

@Entity
@Table(name = "ubicacion_usuario")
public class UbicacionUsuario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // UNIQUE en DB
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "ubicacion", columnDefinition = "GEOGRAPHY(Point,4326)")
    private Point ubicacion;

    @Column(name = "precision_metros")
    private Integer precisionMetros;

    @Column(name = "ultima_actualizacion")
    private OffsetDateTime ultimaActualizacion;

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Point getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Point ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Integer getPrecisionMetros() {
        return precisionMetros;
    }

    public void setPrecisionMetros(Integer precisionMetros) {
        this.precisionMetros = precisionMetros;
    }

    public OffsetDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(OffsetDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    private static final long serialVersionUID = 1L;
}
