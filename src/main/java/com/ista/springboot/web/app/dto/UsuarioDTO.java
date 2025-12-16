package com.ista.springboot.web.app.dto;


import com.ista.springboot.web.app.models.entity.Usuario;
import com.ista.springboot.web.app.models.entity.UsuarioComunidad;
import java.time.OffsetDateTime;

public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String fotoUrl;
    private OffsetDateTime fechaRegistro;
    private OffsetDateTime ultimoAcceso;
    private Boolean activo;
    
    // Datos de la comunidad (sin circular reference)
    private Long comunidadId;
    private String comunidadNombre;
    private String rol;
    private String estadoEnComunidad;

    // Constructor vacío
    public UsuarioDTO() {}

    // Constructor desde Usuario + UsuarioComunidad
    public UsuarioDTO(Usuario usuario, UsuarioComunidad usuarioComunidad) {
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.apellido = usuario.getApellido();
        this.email = usuario.getEmail();
        this.telefono = usuario.getTelefono();
        this.fotoUrl = usuario.getFotoUrl();
        this.fechaRegistro = usuario.getFechaRegistro();
        this.ultimoAcceso = usuario.getUltimoAcceso();
        this.activo = usuario.getActivo();
        
        if (usuarioComunidad != null) {
            this.comunidadId = usuarioComunidad.getComunidad().getId();
            this.comunidadNombre = usuarioComunidad.getComunidad().getNombre();
            this.rol = usuarioComunidad.getRol();
            this.estadoEnComunidad = usuarioComunidad.getEstado();
        }
    }

    // Constructor simplificado solo con Usuario
    public UsuarioDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.apellido = usuario.getApellido();
        this.email = usuario.getEmail();
        this.telefono = usuario.getTelefono();
        this.fotoUrl = usuario.getFotoUrl();
        this.fechaRegistro = usuario.getFechaRegistro();
        this.ultimoAcceso = usuario.getUltimoAcceso();
        this.activo = usuario.getActivo();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public OffsetDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(OffsetDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public OffsetDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(OffsetDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }

    
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Long getComunidadId() { return comunidadId; }
    public void setComunidadId(Long comunidadId) { this.comunidadId = comunidadId; }

    public String getComunidadNombre() { return comunidadNombre; }
    public void setComunidadNombre(String comunidadNombre) { this.comunidadNombre = comunidadNombre; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getEstadoEnComunidad() { return estadoEnComunidad; }
    public void setEstadoEnComunidad(String estadoEnComunidad) { this.estadoEnComunidad = estadoEnComunidad; }
}