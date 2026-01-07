package com.ista.springboot.web.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IncidenteDTO {

    // ======================
    // CAMPOS BÁSICOS
    // ======================
    private String tipo;
    private String descripcion;

    private Double lat;
    private Double lng;

    private String imagenUrl;
    private String videoUrl;
    private String audioUrl;

    private String nivelPrioridad;

    private Long usuarioId;
    private Long comunidadId;

    private UsuarioRef usuario;
    private ComunidadRef comunidad;

    // ======================
    // CAMPOS IA
    // ======================
    private String aiCategoria;
    private String aiPrioridad;
    private Double aiConfianza;
    private Boolean aiPosibleFalso;
    private String aiMotivos;
    private String aiRiesgos;
    private String aiAccionRecomendada;

    // ======================
    // OFFLINE / SYNC (CASO A)
    // ======================
    private String clientGeneratedId;     // UUID recomendado
    private String canalEnvio;           // ONLINE | OFFLINE_SMS | OFFLINE_QUEUE
    private Boolean smsEnviadoPorCliente;

    public IncidenteDTO() {}

    // ===== GETTERS/SETTERS =====
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public String getNivelPrioridad() { return nivelPrioridad; }
    public void setNivelPrioridad(String nivelPrioridad) { this.nivelPrioridad = nivelPrioridad; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Long getComunidadId() { return comunidadId; }
    public void setComunidadId(Long comunidadId) { this.comunidadId = comunidadId; }

    public UsuarioRef getUsuario() { return usuario; }
    public void setUsuario(UsuarioRef usuario) { this.usuario = usuario; }

    public ComunidadRef getComunidad() { return comunidad; }
    public void setComunidad(ComunidadRef comunidad) { this.comunidad = comunidad; }

    // ===== IA =====
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

    // ===== OFFLINE =====
    public String getClientGeneratedId() { return clientGeneratedId; }
    public void setClientGeneratedId(String clientGeneratedId) { this.clientGeneratedId = clientGeneratedId; }

    public String getCanalEnvio() { return canalEnvio; }
    public void setCanalEnvio(String canalEnvio) { this.canalEnvio = canalEnvio; }

    public Boolean getSmsEnviadoPorCliente() { return smsEnviadoPorCliente; }
    public void setSmsEnviadoPorCliente(Boolean smsEnviadoPorCliente) { this.smsEnviadoPorCliente = smsEnviadoPorCliente; }

    // ===== CLASES INTERNAS =====
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UsuarioRef {
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ComunidadRef {
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }
}
