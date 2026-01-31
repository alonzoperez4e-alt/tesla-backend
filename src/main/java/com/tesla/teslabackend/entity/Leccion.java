package com.tesla.teslabackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "leccion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_leccion")
    private Integer idLeccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_semana")
    @ToString.Exclude
    private Semana semana;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private Integer orden;

    @OneToMany(mappedBy = "leccion", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Pregunta> preguntas;
}