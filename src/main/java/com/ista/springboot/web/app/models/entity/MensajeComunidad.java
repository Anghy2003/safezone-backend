package com.ista.springboot.web.app.models.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "mensaje_comunidad")
public class MensajeComunidad implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comunidad_id", nullable = false)
    private Comunidad comunidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column
    private String mensaje;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(length = 30)
    private String tipo = "texto";

    @Column
    private Boolean moderado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderado_por")
    private Usuario moderadoPor;

    @Column(name = "fecha_envio")
    private OffsetDateTime fechaEnvio;

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Comunidad getComunidad() {
        return comunidad;
    }

    public void setComunidad(Comunidad comunidad) {
        this.comunidad = comunidad;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Boolean getModerado() {
        return moderado;
    }

    public void setModerado(Boolean moderado) {
        this.moderado = moderado;
    }

    public Usuario getModeradoPor() {
        return moderadoPor;
    }

    public void setModeradoPor(Usuario moderadoPor) {
        this.moderadoPor = moderadoPor;
    }

    public OffsetDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(OffsetDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    private static final long serialVersionUID = 1L;
}
