package com.tesla.teslabackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;


@Embeddable // Parte 1: Definir la clave compuesta
@Data
@NoArgsConstructor
@AllArgsConstructor
class ProgresoLeccionesId implements Serializable {
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "id_leccion")
    private Integer idLeccion;
}