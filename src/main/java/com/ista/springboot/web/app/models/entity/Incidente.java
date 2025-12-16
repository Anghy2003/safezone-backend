package com.ista.springboot.web.app.models.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;

import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@JsonIgnoreProperties({
        "hibernateLazyInitializer",
        "handler"
})
@Entity
@Table(name = "incidente")
public class Incidente implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 👤 Usuario que reporta el incidente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler",
            "passwordHash",
            "fcmToken",
            "ultimoAcceso",
            "fechaRegistro",
            "activo",
            "comunidad",
            "usuarioComunidades",
            "incidentes",
            "notificaciones",
            "mensajesComunidad"
    })
    private Usuario usuario;

    // 🏘 Comunidad a la que pertenece el incidente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comunidad_id")
    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler",
            "usuarios",
            "incidentes",
            "notificaciones",
            "mensajesComunidad",
            "usuarioComunidades",
            "codigoAcceso",
            "fechaCreacion"
    })
    private Comunidad comunidad;

    @Column(nullable = false, length = 80)
    private String tipo;

    @Column
    private String descripcion;

    // 📍 Geometría en la BD, pero ignorada en JSON
    @JsonIgnore
    @Column(name = "ubicacion", columnDefinition = "GEOGRAPHY(Point,4326)")
    private Point ubicacion;

    // 🖼 Imagen principal (Cloudinary)
    @Column(name = "imagen_url")
    private String imagenUrl;

    // 🎥 Video (Cloudinary)
    @Column(name = "video_url")
    private String videoUrl;

    // 🎙 Audio (Cloudinary)
    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "nivel_prioridad", length = 30)
    private String nivelPrioridad;

    @Column(length = 30)
    private String estado = "pendiente";

    // 👮 Usuario moderador
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderado_por")
    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler",
            "passwordHash",
            "fcmToken",
            "ultimoAcceso",
            "fechaRegistro",
            "activo",
            "comunidad",
            "usuarioComunidades",
            "incidentes",
            "notificaciones",
            "mensajesComunidad"
    })
    private Usuario moderadoPor;

    @Column(name = "fecha_creacion")
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_resolucion")
    private OffsetDateTime fechaResolucion;

    private static final long serialVersionUID = 1L;
    
 // ===================== IA (xAI / Grok) =====================

    @Column(name = "ai_categoria", length = 80)
    private String aiCategoria;

    @Column(name = "ai_prioridad", length = 20)
    private String aiPrioridad; // ALTA / MEDIA / BAJA

    @Column(name = "ai_confianza")
    private Double aiConfianza;

    @Column(name = "ai_posible_falso")
    private Boolean aiPosibleFalso;

    @Column(name = "ai_motivos", columnDefinition = "TEXT")
    private String aiMotivos; // JSON String

    @Column(name = "ai_riesgos", columnDefinition = "TEXT")
    private String aiRiesgos; // JSON String

    @Column(name = "ai_accion_recomendada", columnDefinition = "TEXT")
    private String aiAccionRecomendada;

    @Column(name = "ai_analizado_en")
    private OffsetDateTime aiAnalizadoEn;

    // ===================== GETTERS & SETTERS ===================== //

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Comunidad getComunidad() { return comunidad; }
    public void setComunidad(Comunidad comunidad) { this.comunidad = comunidad; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Point getUbicacion() { return ubicacion; }
    public void setUbicacion(Point ubicacion) { this.ubicacion = ubicacion; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public String getNivelPrioridad() { return nivelPrioridad; }
    public void setNivelPrioridad(String nivelPrioridad) { this.nivelPrioridad = nivelPrioridad; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Usuario getModeradoPor() { return moderadoPor; }
    public void setModeradoPor(Usuario moderadoPor) { this.moderadoPor = moderadoPor; }

    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public OffsetDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(OffsetDateTime fechaResolucion) { this.fechaResolucion = fechaResolucion; }
    public String getAiCategoria() { return aiCategoria; }
    public void setAiCategoria(String aiCategoria) { this.aiCategoria = aiCategoria; }

    public String getAiPrioridad() { return aiPrioridad; }
    public void setAiPrioridad(String aiPrioridad) { this.aiPrioridad = aiPrioridad; }

    public Double getAiConfianza() { return aiConfianza; }
    public void setAiConfianza(Double aiConfianza) { this.aiConfianza = aiConfianza; }

    public Boolean getAiPosibleFalso() { return aiPosibleFalso; }
    public void setAiPosibleFalso(Boolean aiPosibleFalso) { this.aiPosibleFalso = aiPosibleFalso; }

    public String getAiMotivos() { return aiMotivos; }
    public void setAiMotivos(String aiMotivos) { this.aiMotivos = aiMotivos; }

    public String getAiRiesgos() { return aiRiesgos; }
    public void setAiRiesgos(String aiRiesgos) { this.aiRiesgos = aiRiesgos; }

    public String getAiAccionRecomendada() { return aiAccionRecomendada; }
    public void setAiAccionRecomendada(String aiAccionRecomendada) { this.aiAccionRecomendada = aiAccionRecomendada; }

    public OffsetDateTime getAiAnalizadoEn() { return aiAnalizadoEn; }
    public void setAiAnalizadoEn(OffsetDateTime aiAnalizadoEn) { this.aiAnalizadoEn = aiAnalizadoEn; }

}
