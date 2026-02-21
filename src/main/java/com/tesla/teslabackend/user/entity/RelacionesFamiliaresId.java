package com.tesla.teslabackend.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelacionesFamiliaresId implements Serializable {
    @Column(name = "id_padre")
    private Integer idPadre;

    @Column(name = "id_hijo")
    private Integer idHijo;
}