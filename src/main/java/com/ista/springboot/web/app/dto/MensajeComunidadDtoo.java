package com.ista.springboot.web.app.dto;

import java.time.OffsetDateTime;

/**
 * DTO alternativo (si lo necesitas).
 * Incluye campos de chat + adjuntos + filtro de contenido sensible.
 */
public class MensajeComunidadDtoo {

    private Long id;

    private Long comunidadId;

    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioFotoUrl;

    private String canal; // COMUNIDAD | VECINOS
    private String tipo;  // texto | sistema | incidente | imagen | video | audio

    private String mensaje;

    private String imagenUrl;
    private String videoUrl;
    private String audioUrl;

    private Long replyToId;

    private OffsetDateTime fechaEnvio;

    // ✅ NUEVO: FILTRO SENSIBLE
    private Boolean contenidoSensible;
    private String sensibilidadMotivo;
    private Double sensibilidadScore;

    // ===================== GETTERS / SETTERS =====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getComunidadId() { return comunidadId; }
    public void setComunidadId(Long comunidadId) { this.comunidadId = comunidadId; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getUsuarioFotoUrl() { return usuarioFotoUrl; }
    public void setUsuarioFotoUrl(String usuarioFotoUrl) { this.usuarioFotoUrl = usuarioFotoUrl; }

    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public Long getReplyToId() { return replyToId; }
    public void setReplyToId(Long replyToId) { this.replyToId = replyToId; }

    public OffsetDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(OffsetDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public Boolean getContenidoSensible() { return contenidoSensible; }
    public void setContenidoSensible(Boolean contenidoSensible) { this.contenidoSensible = contenidoSensible; }

    public String getSensibilidadMotivo() { return sensibilidadMotivo; }
    public void setSensibilidadMotivo(String sensibilidadMotivo) { this.sensibilidadMotivo = sensibilidadMotivo; }

    public Double getSensibilidadScore() { return sensibilidadScore; }
    public void setSensibilidadScore(Double sensibilidadScore) { this.sensibilidadScore = sensibilidadScore; }
}
