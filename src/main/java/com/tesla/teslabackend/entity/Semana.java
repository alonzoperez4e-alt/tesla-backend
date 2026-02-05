package com.tesla.teslabackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @JsonBackReference
    private Curso curso;

    @Column(name = "nro_semana", nullable = false)
    private Integer nroSemana;

    @Column(name = "is_bloqueada")
    @Builder.Default
    private Boolean isBloqueada = true;

    @OneToMany(mappedBy = "semana", cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonManagedReference
    private List<Leccion> lecciones;
}
