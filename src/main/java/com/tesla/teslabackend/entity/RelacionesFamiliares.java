package com.tesla.teslabackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "relaciones_familiares")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelacionesFamiliares {

    @EmbeddedId
    private RelacionesFamiliaresId id;

    @ManyToOne
    @MapsId("idPadre")
    @JoinColumn(name = "id_padre")
    private Usuario padre;

    @ManyToOne
    @MapsId("idHijo")
    @JoinColumn(name = "id_hijo")
    private Usuario hijo;
}