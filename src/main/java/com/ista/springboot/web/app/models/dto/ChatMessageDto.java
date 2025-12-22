package com.ista.springboot.web.app.models.dto;

public class ChatMessageDto {

    private Long usuarioId;      // quién envía
    private Long comunidadId;    // comunidad destino

    /**
     * Canal del chat:
     * - COMUNIDAD
     * - VECINOS
     */
    private String canal;

    /**
     * Tipo de mensaje:
     * - texto (default)
     * - sistema
     * - incidente (opcional a futuro)
     */
    private String tipo;

    // ===================== CONTENIDO =====================
    private String mensaje;      // texto del mensaje

    private String imagenUrl;    // Cloudinary / S3
    private String videoUrl;
    private String audioUrl;

    // Respuesta a otro mensaje (thread / reply)
    private Long replyToId;

    // ===================== GETTERS / SETTERS =====================

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getComunidadId() {
        return comunidadId;
    }

    public void setComunidadId(Long comunidadId) {
        this.comunidadId = comunidadId;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
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

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public Long getReplyToId() {
        return replyToId;
    }

    public void setReplyToId(Long replyToId) {
        this.replyToId = replyToId;
    }
}
