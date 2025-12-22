package com.ista.springboot.web.app.models.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "comunidad")
public class Comunidad implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String direccion;

    @Column(name = "codigo_acceso", length = 10, nullable = true, unique = true)
    private String codigoAcceso;

    // ✅ NUEVO: Foto referencial de comunidad
    @Column(name = "foto_url", columnDefinition = "TEXT")
    private String fotoUrl;

    @Column(name = "centro_geografico", columnDefinition = "GEOGRAPHY(Point,4326)")
    private Point centroGeografico;

    @Column(name = "radio_km", precision = 6, scale = 2)
    private BigDecimal radioKm = BigDecimal.valueOf(1.00);

    @Column(nullable = false)
    private Boolean activa = true;

    @Column(name = "fecha_creacion")
    private OffsetDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoComunidad estado = EstadoComunidad.ACTIVA;

    // ✅ NUEVO: quién solicitó (para SMS)
    @Column(name = "solicitada_por_usuario_id")
    private Long solicitadaPorUsuarioId;

    @Transient
    private Long miembrosCount;

    // =====================================================
    // 🔗 RELACIÓN Comunidad -> UsuarioComunidad
    // =====================================================
    @OneToMany(mappedBy = "comunidad", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"comunidad", "usuario"})
    private Set<UsuarioComunidad> usuarioComunidades = new HashSet<>();

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = OffsetDateTime.now();
    }

    // GETTERS / SETTERS
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

    public Point getCentroGeografico() { return centroGeografico; }
    public void setCentroGeografico(Point centroGeografico) { this.centroGeografico = centroGeografico; }

    public BigDecimal getRadioKm() { return radioKm; }
    public void setRadioKm(BigDecimal radioKm) { this.radioKm = radioKm; }

    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }

    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public EstadoComunidad getEstado() { return estado; }
    public void setEstado(EstadoComunidad estado) { this.estado = estado; }

    public Long getSolicitadaPorUsuarioId() { return solicitadaPorUsuarioId; }
    public void setSolicitadaPorUsuarioId(Long solicitadaPorUsuarioId) {
        this.solicitadaPorUsuarioId = solicitadaPorUsuarioId;
    }

    public Long getMiembrosCount() { return miembrosCount; }
    public void setMiembrosCount(Long miembrosCount) { this.miembrosCount = miembrosCount; }

    public Set<UsuarioComunidad> getUsuarioComunidades() { return usuarioComunidades; }
    public void setUsuarioComunidades(Set<UsuarioComunidad> usuarioComunidades) {
        this.usuarioComunidades = usuarioComunidades;
    }

    private static final long serialVersionUID = 1L;
}
