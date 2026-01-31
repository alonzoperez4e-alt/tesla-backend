package com.tesla.teslabackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity // Parte 2: La entidad que usa la clave
@Table(name = "progreso_lecciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgresoLecciones {

    @EmbeddedId
    private ProgresoLeccionesId id;

    @ManyToOne
    @MapsId("idUsuario") // Mapea la parte de la clave a la relación
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne
    @MapsId("idLeccion")
    @JoinColumn(name = "id_leccion")
    private Leccion leccion;

    @Builder.Default
    private Boolean completada = false;

    @Column(name = "progreso_porcentaje")
    @Builder.Default
    private Integer progresoPorcentaje = 0;
}