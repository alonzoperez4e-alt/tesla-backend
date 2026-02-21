package com.tesla.teslabackend.progress.entity;

import java.io.Serializable;
import java.util.Objects;

public class ProgresoLeccionesId implements Serializable {

    // Deben ser Integer para coincidir con el tipo de ID de tus entidades Usuario y Leccion
    // Los nombres 'usuario' y 'leccion' deben coincidir con los atributos de la entidad ProgresoLecciones
    private Integer usuario;
    private Integer leccion;

    public ProgresoLeccionesId() {
    }

    public ProgresoLeccionesId(Integer usuario, Integer leccion) {
        this.usuario = usuario;
        this.leccion = leccion;
    }

    public Integer getUsuario() {
        return usuario;
    }

    public void setUsuario(Integer usuario) {
        this.usuario = usuario;
    }

    public Integer getLeccion() {
        return leccion;
    }

    public void setLeccion(Integer leccion) {
        this.leccion = leccion;
    }

    // --- ESTA ES LA SOLUCIÓN AL BUG ---
    // Hibernate usa esto para saber si dos filas son la misma o diferentes
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgresoLeccionesId that = (ProgresoLeccionesId) o;
        return Objects.equals(usuario, that.usuario) &&
                Objects.equals(leccion, that.leccion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuario, leccion);
    }
}