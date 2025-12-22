package com.ista.springboot.web.app.dto;

/**
 * DTO para crear notificaciones (push / comunidad / vecinos).
 * Usado para alertas, incidentes, mensajes del sistema, etc.
 */
public class NotificacionCreateDTO {

    // ===================== IDENTIDAD =====================
    private Long usuarioId;
    private Long comunidadId;

    /**
     * Tipo de notificación:
     * - INCIDENTE_COMUNIDAD
     * - INCIDENTE_VECINOS
     * - MENSAJE_COMUNIDAD
     * - SISTEMA
     * - ALERTA
     */
    private String tipoNotificacion;

    private String titulo;
    private String mensaje;

    // ===================== MULTIMEDIA =====================
    private Boolean tieneFoto;
    private Boolean tieneVideo;
    private Boolean tieneAudio;

    // ===================== UBICACIÓN =====================
    private Boolean tieneUbicacion;
    private Double latitud;
    private Double longitud;
    private String direccion;

    // ===================== FILTRO CONTENIDO SENSIBLE =====================
    /**
     * Indica si el contenido fue marcado como sensible
     * (por IA, Flutter on-device o backend).
     */
    private Boolean contenidoSensible;

    /**
     * Motivo del marcado:
     * - nudity
     * - violence
     * - blood
     * - audio_distress
     * - unknown
     */
    private String sensibilidadMotivo;

    /**
     * Score/confianza del análisis (0.0 – 1.0).
     */
    private Double sensibilidadScore;

    // ===================== GETTERS / SETTERS =====================

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Long getComunidadId() { return comunidadId; }
    public void setComunidadId(Long comunidadId) { this.comunidadId = comunidadId; }

    public String getTipoNotificacion() { return tipoNotificacion; }
    public void setTipoNotificacion(String tipoNotificacion) { this.tipoNotificacion = tipoNotificacion; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    // -------- Multimedia --------
    public Boolean getTieneFoto() { return tieneFoto; }
    public void setTieneFoto(Boolean tieneFoto) { this.tieneFoto = tieneFoto; }

    public Boolean getTieneVideo() { return tieneVideo; }
    public void setTieneVideo(Boolean tieneVideo) { this.tieneVideo = tieneVideo; }

    public Boolean getTieneAudio() { return tieneAudio; }
    public void setTieneAudio(Boolean tieneAudio) { this.tieneAudio = tieneAudio; }

    // -------- Ubicación --------
    public Boolean getTieneUbicacion() { return tieneUbicacion; }
    public void setTieneUbicacion(Boolean tieneUbicacion) { this.tieneUbicacion = tieneUbicacion; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    // -------- Contenido sensible --------
    public Boolean getContenidoSensible() { return contenidoSensible; }
    public void setContenidoSensible(Boolean contenidoSensible) { this.contenidoSensible = contenidoSensible; }

    public String getSensibilidadMotivo() { return sensibilidadMotivo; }
    public void setSensibilidadMotivo(String sensibilidadMotivo) { this.sensibilidadMotivo = sensibilidadMotivo; }

    public Double getSensibilidadScore() { return sensibilidadScore; }
    public void setSensibilidadScore(Double sensibilidadScore) { this.sensibilidadScore = sensibilidadScore; }
}
