package com.ista.springboot.web.app.dto;

import com.ista.springboot.web.app.models.entity.Incidente;
import java.time.OffsetDateTime;

public class IncidenteResponseDTO {

    private Long id;
    private String tipo;
    private String descripcion;
    private Double lat;
    private Double lng;

    private String imagenUrl;
    private String videoUrl;
    private String audioUrl;

    private String nivelPrioridad;
    private String estado;

    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioFoto;

    private Long comunidadId;
    private String comunidadNombre;

    private Long moderadoPorId;
    private String moderadoPorNombre;

    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaResolucion;

    // IA
    private String aiCategoria;
    private String aiPrioridad;
    private Double aiConfianza;
    private Boolean aiPosibleFalso;
    private String aiMotivos;
    private String aiRiesgos;
    private String aiAccionRecomendada;
    private OffsetDateTime aiAnalizadoEn;

    // OFFLINE / SYNC
    private String clientGeneratedId;
    private String canalEnvio;
    private Boolean smsEnviadoPorCliente;

    public IncidenteResponseDTO() {}

    public IncidenteResponseDTO(Incidente incidente) {
        if (incidente == null) return;

        this.id = incidente.getId();
        this.tipo = incidente.getTipo();
        this.descripcion = incidente.getDescripcion();

        if (incidente.getUbicacion() != null) {
            this.lat = incidente.getUbicacion().getY();
            this.lng = incidente.getUbicacion().getX();
        }

        this.imagenUrl = incidente.getImagenUrl();
        this.videoUrl = incidente.getVideoUrl();
        this.audioUrl = incidente.getAudioUrl();

        this.nivelPrioridad = incidente.getNivelPrioridad();
        this.estado = incidente.getEstado();

        if (incidente.getUsuario() != null) {
            this.usuarioId = incidente.getUsuario().getId();
            this.usuarioNombre = incidente.getUsuario().getNombre();
            this.usuarioFoto = incidente.getUsuario().getFotoUrl();
        }

        if (incidente.getComunidad() != null) {
            this.comunidadId = incidente.getComunidad().getId();
            this.comunidadNombre = incidente.getComunidad().getNombre();
        }

        if (incidente.getModeradoPor() != null) {
            this.moderadoPorId = incidente.getModeradoPor().getId();
            this.moderadoPorNombre = incidente.getModeradoPor().getNombre();
        }

        this.fechaCreacion = incidente.getFechaCreacion();
        this.fechaResolucion = incidente.getFechaResolucion();

        // IA
        this.aiCategoria = incidente.getAiCategoria();
        this.aiPrioridad = incidente.getAiPrioridad();
        this.aiConfianza = incidente.getAiConfianza();
        this.aiPosibleFalso = incidente.getAiPosibleFalso();
        this.aiMotivos = incidente.getAiMotivos();
        this.aiRiesgos = incidente.getAiRiesgos();
        this.aiAccionRecomendada = incidente.getAiAccionRecomendada();
        this.aiAnalizadoEn = incidente.getAiAnalizadoEn();

        // OFFLINE
        this.clientGeneratedId = incidente.getClientGeneratedId();
        this.canalEnvio = incidente.getCanalEnvio();
        this.smsEnviadoPorCliente = incidente.getSmsEnviadoPorCliente();
        
        
        
        
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public Double getLng() {
		return lng;
	}

	public void setLng(Double lng) {
		this.lng = lng;
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

	public String getNivelPrioridad() {
		return nivelPrioridad;
	}

	public void setNivelPrioridad(String nivelPrioridad) {
		this.nivelPrioridad = nivelPrioridad;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(Long usuarioId) {
		this.usuarioId = usuarioId;
	}

	public String getUsuarioNombre() {
		return usuarioNombre;
	}

	public void setUsuarioNombre(String usuarioNombre) {
		this.usuarioNombre = usuarioNombre;
	}

	public String getUsuarioFoto() {
		return usuarioFoto;
	}

	public void setUsuarioFoto(String usuarioFoto) {
		this.usuarioFoto = usuarioFoto;
	}

	public Long getComunidadId() {
		return comunidadId;
	}

	public void setComunidadId(Long comunidadId) {
		this.comunidadId = comunidadId;
	}

	public String getComunidadNombre() {
		return comunidadNombre;
	}

	public void setComunidadNombre(String comunidadNombre) {
		this.comunidadNombre = comunidadNombre;
	}

	public Long getModeradoPorId() {
		return moderadoPorId;
	}

	public void setModeradoPorId(Long moderadoPorId) {
		this.moderadoPorId = moderadoPorId;
	}

	public String getModeradoPorNombre() {
		return moderadoPorNombre;
	}

	public void setModeradoPorNombre(String moderadoPorNombre) {
		this.moderadoPorNombre = moderadoPorNombre;
	}

	public OffsetDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(OffsetDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public OffsetDateTime getFechaResolucion() {
		return fechaResolucion;
	}

	public void setFechaResolucion(OffsetDateTime fechaResolucion) {
		this.fechaResolucion = fechaResolucion;
	}

	public String getAiCategoria() {
		return aiCategoria;
	}

	public void setAiCategoria(String aiCategoria) {
		this.aiCategoria = aiCategoria;
	}

	public String getAiPrioridad() {
		return aiPrioridad;
	}

	public void setAiPrioridad(String aiPrioridad) {
		this.aiPrioridad = aiPrioridad;
	}

	public Double getAiConfianza() {
		return aiConfianza;
	}

	public void setAiConfianza(Double aiConfianza) {
		this.aiConfianza = aiConfianza;
	}

	public Boolean getAiPosibleFalso() {
		return aiPosibleFalso;
	}

	public void setAiPosibleFalso(Boolean aiPosibleFalso) {
		this.aiPosibleFalso = aiPosibleFalso;
	}

	public String getAiMotivos() {
		return aiMotivos;
	}

	public void setAiMotivos(String aiMotivos) {
		this.aiMotivos = aiMotivos;
	}

	public String getAiRiesgos() {
		return aiRiesgos;
	}

	public void setAiRiesgos(String aiRiesgos) {
		this.aiRiesgos = aiRiesgos;
	}

	public String getAiAccionRecomendada() {
		return aiAccionRecomendada;
	}

	public void setAiAccionRecomendada(String aiAccionRecomendada) {
		this.aiAccionRecomendada = aiAccionRecomendada;
	}

	public OffsetDateTime getAiAnalizadoEn() {
		return aiAnalizadoEn;
	}

	public void setAiAnalizadoEn(OffsetDateTime aiAnalizadoEn) {
		this.aiAnalizadoEn = aiAnalizadoEn;
	}

	public String getClientGeneratedId() {
		return clientGeneratedId;
	}

	public void setClientGeneratedId(String clientGeneratedId) {
		this.clientGeneratedId = clientGeneratedId;
	}

	public String getCanalEnvio() {
		return canalEnvio;
	}

	public void setCanalEnvio(String canalEnvio) {
		this.canalEnvio = canalEnvio;
	}

	public Boolean getSmsEnviadoPorCliente() {
		return smsEnviadoPorCliente;
	}

	public void setSmsEnviadoPorCliente(Boolean smsEnviadoPorCliente) {
		this.smsEnviadoPorCliente = smsEnviadoPorCliente;
	}

  
}
