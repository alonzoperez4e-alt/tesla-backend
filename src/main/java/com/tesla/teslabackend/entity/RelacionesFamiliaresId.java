package com.tesla.teslabackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
class RelacionesFamiliaresId implements Serializable {
    @Column(name = "id_padre")
    private Integer idPadre;

    @Column(name = "id_hijo")
    private Integer idHijo;
}