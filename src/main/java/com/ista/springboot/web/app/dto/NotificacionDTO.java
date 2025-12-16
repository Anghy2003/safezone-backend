package com.ista.springboot.web.app.dto;

import com.ista.springboot.web.app.models.entity.Notificacion;
import java.time.OffsetDateTime;

public class NotificacionDTO {
    private Long id;

    // 👇 Usuario
    private Long usuarioId;
    private String nombreUsuario;
    private String fotoUrl;   // NUEVO

    // Incidente
    private Long incidenteId;
    private String incidenteTipo;

    // Comunidad
    private Long comunidadId;
    private String comunidadNombre;

    private String tipoNotificacion;
    private String titulo;
    private String mensaje;
    private Boolean leido;
    private Boolean enviado;
    private OffsetDateTime fechaEnvio;
    private OffsetDateTime fechaLectura;

    // Multimedia
    private Boolean tieneFoto;
    private Boolean tieneVideo;
    private Boolean tieneAudio;
    private Boolean tieneUbicacion;
    private Double latitud;
    private Double longitud;
    private String direccion;

    // Constructor vacío
    public NotificacionDTO() {}

    // Constructor desde entidad
    public NotificacionDTO(Notificacion notificacion) {
        this.id = notificacion.getId();

        // Usuario
        if (notificacion.getUsuario() != null) {
            this.usuarioId = notificacion.getUsuario().getId();
            this.nombreUsuario = notificacion.getUsuario().getNombre();
            this.fotoUrl = notificacion.getUsuario().getFotoUrl(); // NUEVO
        }

        // Incidente
        if (notificacion.getIncidente() != null) {
            this.incidenteId = notificacion.getIncidente().getId();
            this.incidenteTipo = notificacion.getIncidente().getTipo();
        }

        // Comunidad
        if (notificacion.getComunidad() != null) {
            this.comunidadId = notificacion.getComunidad().getId();
            this.comunidadNombre = notificacion.getComunidad().getNombre();
        }

        this.tipoNotificacion = notificacion.getTipoNotificacion();
        this.titulo = notificacion.getTitulo();
        this.mensaje = notificacion.getMensaje();
        this.leido = notificacion.getLeido();
        this.enviado = notificacion.getEnviado();
        this.fechaEnvio = notificacion.getFechaEnvio();
        this.fechaLectura = notificacion.getFechaLectura();

        // Multimedia
        this.tieneFoto = notificacion.getTieneFoto();
        this.tieneVideo = notificacion.getTieneVideo();
        this.tieneAudio = notificacion.getTieneAudio();
        this.tieneUbicacion = notificacion.getTieneUbicacion();
        this.latitud = notificacion.getLatitud();
        this.longitud = notificacion.getLongitud();
        this.direccion = notificacion.getDireccion();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public Long getIncidenteId() { return incidenteId; }
    public void setIncidenteId(Long incidenteId) { this.incidenteId = incidenteId; }

    public String getIncidenteTipo() { return incidenteTipo; }
    public void setIncidenteTipo(String incidenteTipo) { this.incidenteTipo = incidenteTipo; }

    public Long getComunidadId() { return comunidadId; }
    public void setComunidadId(Long comunidadId) { this.comunidadId = comunidadId; }

    public String getComunidadNombre() { return comunidadNombre; }
    public void setComunidadNombre(String comunidadNombre) { this.comunidadNombre = comunidadNombre; }

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
