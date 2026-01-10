package com.ista.springboot.web.app.models.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(
    name = "usuario_comunidad",
    uniqueConstraints = @UniqueConstraint(columnNames = { "usuario_id", "comunidad_id" })
)
public class UsuarioComunidad implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================================
    // 🔹 USUARIO
    // =====================================================
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({
        "usuarioComunidades",
        "passwordHash"
    })
    private Usuario usuario;

    // =====================================================
    // 🔹 COMUNIDAD
    // =====================================================
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comunidad_id", nullable = false)
    @JsonIgnoreProperties({
        "usuarioComunidades",
        "usuarios",
        "incidentes"
    })
    private Comunidad comunidad;

    // =====================================================
    // 🔹 ROL
    // =====================================================
    // Alineado a tu service: super_admin / admin_comunidad / usuario
    @Column(length = 30, nullable = false)
    private String rol = "usuario";

    // =====================================================
    // 🔹 ESTADO: pendiente / activo / expulsado
    // =====================================================
    @Column(length = 30, nullable = false)
    private String estado = "pendiente";

    // =====================================================
    // 🔹 APROBADO POR (otro usuario)
    // =====================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobado_por")
    @JsonIgnoreProperties({
        "usuarioComunidades",
        "passwordHash"
    })
    private Usuario aprobadoPor;

    // =====================================================
    // 🔹 FECHA DE UNIÓN
    // =====================================================
    @Column(name = "fecha_union")
    private OffsetDateTime fechaUnion;

    @PrePersist
    public void prePersist() {
        if (fechaUnion == null) {
            fechaUnion = OffsetDateTime.now();
        }
        if (rol == null || rol.trim().isEmpty()) {
            rol = "usuario";
        }
        if (estado == null || estado.trim().isEmpty()) {
            estado = "pendiente";
        }
    }

    // =====================================================
    // GETTERS & SETTERS
    // =====================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Comunidad getComunidad() { return comunidad; }
    public void setComunidad(Comunidad comunidad) { this.comunidad = comunidad; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Usuario getAprobadoPor() { return aprobadoPor; }
    public void setAprobadoPor(Usuario aprobadoPor) { this.aprobadoPor = aprobadoPor; }

    public OffsetDateTime getFechaUnion() { return fechaUnion; }
    public void setFechaUnion(OffsetDateTime fechaUnion) { this.fechaUnion = fechaUnion; }

    private static final long serialVersionUID = 1L;
}
