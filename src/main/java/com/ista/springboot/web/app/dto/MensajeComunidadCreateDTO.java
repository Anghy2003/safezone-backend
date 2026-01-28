package com.ista.springboot.web.app.dto;

/**
 * DTO de entrada para enviar mensajes por WebSocket (y también por REST).
 * Incluye adjuntos y banderas de contenido sensible.
 *
 * ✅ Para NEARBY (GPS):
 * - lat, lng, radio (opcionales; requeridos solo cuando canal=NEARBY o destino /chat/nearby)
 */
public class MensajeComunidadCreateDTO {

    private Long comunidadId;
    private Long usuarioId;

    private String canal; // COMUNIDAD | VECINOS | NEARBY
    private String tipo;  // texto | imagen | video | audio | sistema | incidente

    private String mensaje;

    private String imagenUrl;
    private String videoUrl;
    private String audioUrl;

    private Long replyToId;

    // ✅ GPS (para usuarios cercanos)
    private Double lat;
    private Double lng;
    private Double radio; // metros (ej: 2000)

    // ===================== FILTRO SENSIBLE =====================
    private Boolean contenidoSensible;
    private String sensibilidadMotivo;
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

    // ✅ GPS getters/setters
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public Double getRadio() { return radio; }
    public void setRadio(Double radio) { this.radio = radio; }

    // Sensible
    public Boolean getContenidoSensible() { return contenidoSensible; }
    public void setContenidoSensible(Boolean contenidoSensible) { this.contenidoSensible = contenidoSensible; }

    public String getSensibilidadMotivo() { return sensibilidadMotivo; }
    public void setSensibilidadMotivo(String sensibilidadMotivo) { this.sensibilidadMotivo = sensibilidadMotivo; }

    public Double getSensibilidadScore() { return sensibilidadScore; }
    public void setSensibilidadScore(Double sensibilidadScore) { this.sensibilidadScore = sensibilidadScore; }

    public boolean isContenidoSensible() {
        return Boolean.TRUE.equals(contenidoSensible);
    }
}
