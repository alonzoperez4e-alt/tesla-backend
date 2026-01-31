package com.tesla.teslabackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estadisticas_alumno")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasAlumno {

    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @OneToOne
    @MapsId // Esto dice: "Mi ID es el mismo que el del Usuario asociado"
    @JoinColumn(name = "id_usuario")
    @ToString.Exclude
    private Usuario usuario;

    @Column(name = "racha_actual")
    @Builder.Default
    private Integer rachaActual = 0;

    @Column(name = "exp_total")
    @Builder.Default
    private Integer expTotal = 0;

    @Column(name = "estado_mascota", length = 20)
    @Builder.Default
    private String estadoMascota = "egg";
}