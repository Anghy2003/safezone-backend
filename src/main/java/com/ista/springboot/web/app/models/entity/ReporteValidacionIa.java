package com.ista.springboot.web.app.models.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "reporte_validacion_ia")
public class ReporteValidacionIa implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incidente_id", nullable = false)
    private Incidente incidente;

    @Column(name = "modelo_ia", length = 100)
    private String modeloIa;

    @Column(name = "tipo_predicho", length = 100)
    private String tipoPredicho;

    @Column(precision = 5, scale = 2)
    private BigDecimal confianza;

    @Column(length = 50)
    private String resultado;

    @Column(name = "razon_deteccion")
    private String razonDeteccion;

    @Column(name = "fecha_analisis")
    private OffsetDateTime fechaAnalisis;

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Incidente getIncidente() {
        return incidente;
    }

    public void setIncidente(Incidente incidente) {
        this.incidente = incidente;
    }

    public String getModeloIa() {
        return modeloIa;
    }

    public void setModeloIa(String modeloIa) {
        this.modeloIa = modeloIa;
    }

    public String getTipoPredicho() {
        return tipoPredicho;
    }

    public void setTipoPredicho(String tipoPredicho) {
        this.tipoPredicho = tipoPredicho;
    }

    public BigDecimal getConfianza() {
        return confianza;
    }

    public void setConfianza(BigDecimal confianza) {
        this.confianza = confianza;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getRazonDeteccion() {
        return razonDeteccion;
    }

    public void setRazonDeteccion(String razonDeteccion) {
        this.razonDeteccion = razonDeteccion;
    }

    public OffsetDateTime getFechaAnalisis() {
        return fechaAnalisis;
    }

    public void setFechaAnalisis(OffsetDateTime fechaAnalisis) {
        this.fechaAnalisis = fechaAnalisis;
    }

    private static final long serialVersionUID = 1L;
}
