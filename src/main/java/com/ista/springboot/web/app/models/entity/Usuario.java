package com.ista.springboot.web.app.models.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String apellido;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "firebase_uid", unique = true, length = 128)
    private String firebaseUid;

    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }


    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "fecha_registro")
    private OffsetDateTime fechaRegistro;

    @Column(name = "ultimo_acceso")
    private OffsetDateTime ultimoAcceso;

    @Column(nullable = false)
    private Boolean activo = true;

    // 🔥 Token FCM
    @Column(name = "fcm_token", length = 500)
    private String fcmToken;

    // 🆕 NUEVO CAMPO — Información del dispositivo
    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    // ============================================================
    // 🔗 RELACIÓN CON USUARIO_COMUNIDAD
    // ============================================================
    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"usuario", "comunidad"})
    private Set<UsuarioComunidad> usuarioComunidades = new HashSet<>();

    private static final long serialVersionUID = 1L;

    // ============================================================
    // GETTERS & SETTERS
    // ============================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public OffsetDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(OffsetDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public OffsetDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(OffsetDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }

    public Set<UsuarioComunidad> getUsuarioComunidades() {
        return usuarioComunidades;
    }
    public void setUsuarioComunidades(Set<UsuarioComunidad> usuarioComunidades) {
        this.usuarioComunidades = usuarioComunidades;
    }
}
