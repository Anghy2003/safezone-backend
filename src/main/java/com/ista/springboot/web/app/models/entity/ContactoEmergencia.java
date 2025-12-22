package com.ista.springboot.web.app.models.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "contacto_emergencia")
public class ContactoEmergencia implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(length = 50)
    private String relacion;

    @Column
    private Integer prioridad = 1;

    @Column(nullable = false)
    private Boolean activo = true;

    // 🔹 NUEVO CAMPO FOTO (URL)
    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(name = "fecha_agregado")
    private OffsetDateTime fechaAgregado;

    // ================= GETTERS & SETTERS =================

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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRelacion() {
        return relacion;
    }

    public void setRelacion(String relacion) {
        this.relacion = relacion;
    }

    public Integer getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Integer prioridad) {
        this.prioridad = prioridad;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public OffsetDateTime getFechaAgregado() {
        return fechaAgregado;
    }

    public void setFechaAgregado(OffsetDateTime fechaAgregado) {
        this.fechaAgregado = fechaAgregado;
    }

    private static final long serialVersionUID = 1L;
}
