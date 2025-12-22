package com.ista.springboot.web.app.dto;

import java.time.OffsetDateTime;

import com.ista.springboot.web.app.models.entity.MensajeComunidad;
import com.ista.springboot.web.app.models.entity.Usuario;

public class MensajeComunidadDTO {

    private Long id;
    private Long comunidadId;

    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioFotoUrl;

    private String canal;
    private String tipo;

    private String mensaje;

    private String imagenUrl;
    private String videoUrl;
    private String audioUrl;

    private Long replyToId;

    private OffsetDateTime fechaEnvio;

    // ✅ NUEVO
    private Boolean contenidoSensible;
    private String sensibilidadMotivo;
    private Double sensibilidadScore;

    public MensajeComunidadDTO() {}

    public MensajeComunidadDTO(MensajeComunidad m) {
        if (m == null) return;

        this.id = m.getId();
        this.comunidadId = (m.getComunidad() != null) ? m.getComunidad().getId() : null;

        Usuario u = m.getUsuario();
        if (u != null) {
            this.usuarioId = u.getId();
            String nombre = (u.getNombre() != null) ? u.getNombre() : "";
            String apellido = (u.getApellido() != null) ? (" " + u.getApellido()) : "";
            this.usuarioNombre = (nombre + apellido).trim();
            this.usuarioFotoUrl = u.getFotoUrl();
        }

        this.canal = m.getCanal();
        this.tipo = m.getTipo();

        this.mensaje = m.getMensaje();

        this.imagenUrl = m.getImagenUrl();
        this.videoUrl = m.getVideoUrl();
        this.audioUrl = m.getAudioUrl();

        this.replyToId = (m.getReplyTo() != null) ? m.getReplyTo().getId() : null;

        this.fechaEnvio = m.getFechaEnvio();

        // ✅ sensible
        this.contenidoSensible = Boolean.TRUE.equals(m.getContenidoSensible());
        this.sensibilidadMotivo = m.getSensibilidadMotivo();
        this.sensibilidadScore = m.getSensibilidadScore();
    }

    // Getters/Setters (incluye los nuevos)
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
