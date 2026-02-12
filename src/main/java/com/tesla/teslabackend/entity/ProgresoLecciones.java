package com.tesla.teslabackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "progreso_lecciones")
@IdClass(ProgresoLeccionesId.class)
public class ProgresoLecciones {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario") // Asegúrate que en BD sea id_usuario
    @JsonIgnore
    private Usuario usuario;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_leccion", referencedColumnName = "id_leccion") // Asegúrate que en BD sea id_leccion
    @JsonIgnore
    private Leccion leccion;

    @Column(name = "completada")
    private Boolean completada = false;

    @Column(name = "progreso_porcentaje")
    private Integer progresoPorcentaje = 0;

    public ProgresoLecciones() {
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Leccion getLeccion() {
        return leccion;
    }

    public void setLeccion(Leccion leccion) {
        this.leccion = leccion;
    }

    public Boolean getCompletada() {
        return completada;
    }

    public void setCompletada(Boolean completada) {
        this.completada = completada;
    }

    public Integer getProgresoPorcentaje() {
        return progresoPorcentaje;
    }

    public void setProgresoPorcentaje(Integer progresoPorcentaje) {
        this.progresoPorcentaje = progresoPorcentaje;
    }
}