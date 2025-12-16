package com.ista.springboot.web.app.models.dto;

public class ChatMessageDto {

    private Long usuarioId;    // quién envía
    private Long comunidadId;  // a qué comunidad pertenece
    private String canal;      // "COMUNIDAD" o "VECINOS"
    private String contenido;  // texto del mensaje

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

    public String getContenido() {
        return contenido;
    }
    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
}