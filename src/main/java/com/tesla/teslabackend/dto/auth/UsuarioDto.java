package com.tesla.teslabackend.dto.auth;

import com.tesla.teslabackend.entity.Rol;

import java.time.LocalDateTime;

public class UsuarioDto {
    private Integer idUsuario;
    private String codigoUsuario;
    private String nombre;
    private String apellido;
    private Rol rol;
    private String area;
    private LocalDateTime fechaRegistro;

    public UsuarioDto(Integer idUsuario, String codigoUsuario, String nombre, String apellido, Rol rol, String area, LocalDateTime fechaRegistro, Boolean estado) {
        this.idUsuario = idUsuario;
        this.codigoUsuario = codigoUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.rol = rol;

        this.area = area;
        this.fechaRegistro = fechaRegistro;
    }

    // Getters y Setters
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }
    public Integer getIdRol() { return idRol; }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }
    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}