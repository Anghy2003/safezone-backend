package com.ista.springboot.web.app.dto;

import java.time.OffsetDateTime;

import com.ista.springboot.web.app.models.entity.UsuarioComunidad;

public class SolicitudJoinDTO {

    private Long usuarioId;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String fotoUrl;

    private Long comunidadId;
    private String estado;
    private String rol;

    private OffsetDateTime fechaUnion;

    public SolicitudJoinDTO() {}

    public SolicitudJoinDTO(UsuarioComunidad uc) {
        if (uc == null) return;

        if (uc.getUsuario() != null) {
            this.usuarioId = uc.getUsuario().getId();
            this.nombre = uc.getUsuario().getNombre();
            this.apellido = uc.getUsuario().getApellido();
            this.email = uc.getUsuario().getEmail();
            this.telefono = uc.getUsuario().getTelefono();
            this.fotoUrl = uc.getUsuario().getFotoUrl();
        }

        if (uc.getComunidad() != null) {
            this.comunidadId = uc.getComunidad().getId();
        }

        this.estado = uc.getEstado();
        this.rol = uc.getRol();
        this.fechaUnion = uc.getFechaUnion();
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public Long getComunidadId() { return comunidadId; }
    public void setComunidadId(Long comunidadId) { this.comunidadId = comunidadId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public OffsetDateTime getFechaUnion() { return fechaUnion; }
    public void setFechaUnion(OffsetDateTime fechaUnion) { this.fechaUnion = fechaUnion; }
}
