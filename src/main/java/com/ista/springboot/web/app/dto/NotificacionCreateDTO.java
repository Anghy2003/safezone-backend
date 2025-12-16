package com.ista.springboot.web.app.dto;

public class NotificacionCreateDTO {
    private Long usuarioId;
    private Long comunidadId;
    private String tipoNotificacion;
    private String titulo;
    private String mensaje;

    // Multimedia opcional
    private Boolean tieneFoto;
    private Boolean tieneVideo;
    private Boolean tieneAudio;
    private Boolean tieneUbicacion;
    private Double latitud;
    private Double longitud;
    private String direccion;
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
	public String getTipoNotificacion() {
		return tipoNotificacion;
	}
	public void setTipoNotificacion(String tipoNotificacion) {
		this.tipoNotificacion = tipoNotificacion;
	}
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public String getMensaje() {
		return mensaje;
	}
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
	public Boolean getTieneFoto() {
		return tieneFoto;
	}
	public void setTieneFoto(Boolean tieneFoto) {
		this.tieneFoto = tieneFoto;
	}
	public Boolean getTieneVideo() {
		return tieneVideo;
	}
	public void setTieneVideo(Boolean tieneVideo) {
		this.tieneVideo = tieneVideo;
	}
	public Boolean getTieneAudio() {
		return tieneAudio;
	}
	public void setTieneAudio(Boolean tieneAudio) {
		this.tieneAudio = tieneAudio;
	}
	public Boolean getTieneUbicacion() {
		return tieneUbicacion;
	}
	public void setTieneUbicacion(Boolean tieneUbicacion) {
		this.tieneUbicacion = tieneUbicacion;
	}
	public Double getLatitud() {
		return latitud;
	}
	public void setLatitud(Double latitud) {
		this.latitud = latitud;
	}
	public Double getLongitud() {
		return longitud;
	}
	public void setLongitud(Double longitud) {
		this.longitud = longitud;
	}
	public String getDireccion() {
		return direccion;
	}
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

    // getters y setters
    
}
