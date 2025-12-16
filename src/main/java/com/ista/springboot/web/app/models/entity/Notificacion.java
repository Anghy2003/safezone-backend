package com.ista.springboot.web.app.models.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "notificacion")
public class Notificacion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 👤 Usuario que genera / recibe la notificación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler",
            // Evitar bucles al serializar usuario → notificaciones / incidentes / comunidades
            "passwordHash",
            "fcmToken",
            "ultimoAcceso",
            "fechaRegistro",
            "activo",
            "comunidad",          // si aún existe el campo
            "usuarioComunidades", // si tienes esta relación
            "incidentes",
            "notificaciones",
            "mensajesComunidad"
    })
    private Usuario usuario;

    // 📌 Incidente relacionado (si aplica)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incidente_id")
    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler",
            "usuario",
            "comunidad",
            "moderadoPor",
            "ubicacion" // ya está ignorado, pero por si acaso
    })
    private Incidente incidente;

    // 🏘 Comunidad asociada a la notificación (para chat o broadcast)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comunidad_id")
    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler",
            // Evitar bucles: comunidad → usuarios / incidentes / notificaciones / mensajes...
            "usuarios",
            "incidentes",
            "notificaciones",
            "mensajesComunidad",
            "usuarioComunidades",
            "codigoAcceso",
            "fechaCreacion"
    })
    private Comunidad comunidad;

    @Column(name = "tipo_notificacion", length = 50)
    private String tipoNotificacion;

    @Column(length = 150)
    private String titulo;

    @Column
    private String mensaje;

    @Column
    private Boolean leido = false;

    @Column
    private Boolean enviado = false;

    @Column(name = "proveedor_msg_id")
    private String proveedorMsgId;

    @Column(name = "error_envio")
    private String errorEnvio;

    @Column(name = "fecha_envio")
    private OffsetDateTime fechaEnvio;

    @Column(name = "fecha_lectura")
    private OffsetDateTime fechaLectura;

    @Column(name = "tiene_foto")
    private Boolean tieneFoto = false;

    @Column(name = "tiene_video")
    private Boolean tieneVideo = false;

    @Column(name = "tiene_audio")
    private Boolean tieneAudio = false;

    @Column(name = "tiene_ubicacion")
    private Boolean tieneUbicacion = false;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    @Column(name = "direccion", length = 200)
    private String direccion;

    private static final long serialVersionUID = 1L;

    // ===================== GETTERS & SETTERS ===================== //

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Incidente getIncidente() { return incidente; }
    public void setIncidente(Incidente incidente) { this.incidente = incidente; }

    public Comunidad getComunidad() { return comunidad; }
    public void setComunidad(Comunidad comunidad) { this.comunidad = comunidad; }

    public String getTipoNotificacion() { return tipoNotificacion; }
    public void setTipoNotificacion(String tipoNotificacion) { this.tipoNotificacion = tipoNotificacion; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Boolean getLeido() { return leido; }
    public void setLeido(Boolean leido) { this.leido = leido; }

    public Boolean getEnviado() { return enviado; }
    public void setEnviado(Boolean enviado) { this.enviado = enviado; }

    public String getProveedorMsgId() { return proveedorMsgId; }
    public void setProveedorMsgId(String proveedorMsgId) { this.proveedorMsgId = proveedorMsgId; }

    public String getErrorEnvio() { return errorEnvio; }
    public void setErrorEnvio(String errorEnvio) { this.errorEnvio = errorEnvio; }

    public OffsetDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(OffsetDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public OffsetDateTime getFechaLectura() { return fechaLectura; }
    public void setFechaLectura(OffsetDateTime fechaLectura) { this.fechaLectura = fechaLectura; }

    public Boolean getTieneFoto() { return tieneFoto; }
    public void setTieneFoto(Boolean tieneFoto) { this.tieneFoto = tieneFoto; }

    public Boolean getTieneVideo() { return tieneVideo; }
    public void setTieneVideo(Boolean tieneVideo) { this.tieneVideo = tieneVideo; }

    public Boolean getTieneAudio() { return tieneAudio; }
    public void setTieneAudio(Boolean tieneAudio) { this.tieneAudio = tieneAudio; }

    public Boolean getTieneUbicacion() { return tieneUbicacion; }
    public void setTieneUbicacion(Boolean tieneUbicacion) { this.tieneUbicacion = tieneUbicacion; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
