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

    // ===================== RELACIONES =====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comunidad_id", nullable = false)
    private Comunidad comunidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private MensajeComunidad replyTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderado_por")
    private Usuario moderadoPor;

    // ===================== CAMPOS =====================

    @Column(columnDefinition = "text")
    private String mensaje;

    @Column(name = "imagen_url", columnDefinition = "text")
    private String imagenUrl;

    @Column(name = "video_url", columnDefinition = "text")
    private String videoUrl;

    @Column(name = "audio_url", columnDefinition = "text")
    private String audioUrl;

    @Column(length = 20, nullable = false)
    private String canal = "COMUNIDAD"; // COMUNIDAD | VECINOS

    @Column(length = 30)
    private String tipo = "texto"; // texto | sistema | incidente | imagen | video | audio

    @Column
    private Boolean moderado = false;

    @Column(name = "fecha_envio")
    private OffsetDateTime fechaEnvio;

    // ===================== NUEVO: FILTRO SENSIBLE =====================
    @Column(name = "contenido_sensible")
    private Boolean contenidoSensible = false;

    @Column(name = "sensibilidad_motivo", length = 60)
    private String sensibilidadMotivo;

    @Column(name = "sensibilidad_score")
    private Double sensibilidadScore;

    // ===================== LIFECYCLE =====================

    @PrePersist
    public void prePersist() {
        if (fechaEnvio == null) fechaEnvio = OffsetDateTime.now();
        if (canal == null || canal.isBlank()) canal = "COMUNIDAD";
        if (tipo == null || tipo.isBlank()) tipo = "texto";
        if (moderado == null) moderado = false;

        if (contenidoSensible == null) contenidoSensible = false;
        if (sensibilidadMotivo != null && sensibilidadMotivo.isBlank()) sensibilidadMotivo = null;
    }

    // ===================== GETTERS / SETTERS =====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Comunidad getComunidad() { return comunidad; }
    public void setComunidad(Comunidad comunidad) { this.comunidad = comunidad; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public MensajeComunidad getReplyTo() { return replyTo; }
    public void setReplyTo(MensajeComunidad replyTo) { this.replyTo = replyTo; }

    public Usuario getModeradoPor() { return moderadoPor; }
    public void setModeradoPor(Usuario moderadoPor) { this.moderadoPor = moderadoPor; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Boolean getModerado() { return moderado; }
    public void setModerado(Boolean moderado) { this.moderado = moderado; }

    public OffsetDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(OffsetDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public Boolean getContenidoSensible() { return contenidoSensible; }
    public void setContenidoSensible(Boolean contenidoSensible) { this.contenidoSensible = contenidoSensible; }

    public String getSensibilidadMotivo() { return sensibilidadMotivo; }
    public void setSensibilidadMotivo(String sensibilidadMotivo) { this.sensibilidadMotivo = sensibilidadMotivo; }

    public Double getSensibilidadScore() { return sensibilidadScore; }
    public void setSensibilidadScore(Double sensibilidadScore) { this.sensibilidadScore = sensibilidadScore; }

    private static final long serialVersionUID = 1L;
}
