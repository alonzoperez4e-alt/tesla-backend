package com.tesla.teslabackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "semana")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Semana {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_semana")
    private Integer idSemana;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_curso")
    @ToString.Exclude
    private Curso curso;

    @Column(name = "nro_semana", nullable = false)
    private Integer nroSemana;

    @Column(name = "is_bloqueada")
    @Builder.Default
    private Boolean isBloqueada = true;

    @OneToMany(mappedBy = "semana", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Leccion> lecciones;
}
