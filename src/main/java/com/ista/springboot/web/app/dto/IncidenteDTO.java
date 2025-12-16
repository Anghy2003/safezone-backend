package com.ista.springboot.web.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO para recibir incidentes desde el frontend (Flutter).
 * Usado en POST /api/incidentes
 *
 * Soporta 2 formatos:
 * 1) usuarioId / comunidadId
 * 2) usuario: { id } / comunidad: { id }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IncidenteDTO {

    private String tipo;
    private String descripcion;

    private Double lat;
    private Double lng;

    private String imagenUrl;
    private String videoUrl;
    private String audioUrl;

    private String nivelPrioridad;

    // ✅ Formato simple
    private Long usuarioId;
    private Long comunidadId;

    // ✅ Formato anidado (para compatibilidad con tu Flutter actual)
    private UsuarioRef usuario;
    private ComunidadRef comunidad;

    // =====================
    // Constructor vacío
    // =====================
    public IncidenteDTO() {}

    // =====================
    // Getters y Setters
    // =====================

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public String getNivelPrioridad() { return nivelPrioridad; }
    public void setNivelPrioridad(String nivelPrioridad) { this.nivelPrioridad = nivelPrioridad; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Long getComunidadId() { return comunidadId; }
    public void setComunidadId(Long comunidadId) { this.comunidadId = comunidadId; }

    // ✅ Estos son los que te faltaban (por eso se veía rojo en tu controller)
    public UsuarioRef getUsuario() { return usuario; }
    public void setUsuario(UsuarioRef usuario) { this.usuario = usuario; }

    public ComunidadRef getComunidad() { return comunidad; }
    public void setComunidad(ComunidadRef comunidad) { this.comunidad = comunidad; }

    // =====================
    // Clases internas para { "usuario": {"id": ...} }
    // =====================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UsuarioRef {
        private Long id;

        public UsuarioRef() {}
        public UsuarioRef(Long id) { this.id = id; }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ComunidadRef {
        private Long id;

        public ComunidadRef() {}
        public ComunidadRef(Long id) { this.id = id; }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }
}
