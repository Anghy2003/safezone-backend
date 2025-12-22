package com.ista.springboot.web.app.dto;

/**
 * DTO de entrada para enviar mensajes por WebSocket (y también por REST).
 * Incluye adjuntos y banderas de contenido sensible.
 */
public class MensajeComunidadCreateDTO {

    private Long comunidadId;
    private Long usuarioId;

    private String canal; // COMUNIDAD | VECINOS
    private String tipo;  // texto | imagen | video | audio | sistema | incidente

    private String mensaje;

    private String imagenUrl;
    private String videoUrl;
    private String audioUrl;

    private Long replyToId;

    // ===================== NUEVO: FILTRO SENSIBLE =====================
    /**
     * true si Flutter/IA marcó el contenido como sensible.
     */
    private Boolean contenidoSensible;

    /**
     * Motivo (opcional): "nudity", "violence", "blood", "unknown", etc.
     */
    private String sensibilidadMotivo;

    /**
     * Score/confianza (opcional): 0.0 - 1.0
     */
    private Double sensibilidadScore;

    public MensajeComunidadCreateDTO() {}

    public Long getComunidadId() { return comunidadId; }
    public void setComunidadId(Long comunidadId) { this.comunidadId = comunidadId; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

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

    // ===================== GETTERS/SETTERS NUEVOS =====================

    public Boolean getContenidoSensible() { return contenidoSensible; }
    public void setContenidoSensible(Boolean contenidoSensible) { this.contenidoSensible = contenidoSensible; }

    public String getSensibilidadMotivo() { return sensibilidadMotivo; }
    public void setSensibilidadMotivo(String sensibilidadMotivo) { this.sensibilidadMotivo = sensibilidadMotivo; }

    public Double getSensibilidadScore() { return sensibilidadScore; }
    public void setSensibilidadScore(Double sensibilidadScore) { this.sensibilidadScore = sensibilidadScore; }

    // ===================== (OPCIONAL) Helpers seguros =====================
    // No afectan el JSON; ayudan a evitar nulls en el backend si algún día lo usas.
    public boolean isContenidoSensible() {
        return Boolean.TRUE.equals(contenidoSensible);
    }
}
