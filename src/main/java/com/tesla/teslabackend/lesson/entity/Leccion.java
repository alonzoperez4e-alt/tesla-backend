package com.tesla.teslabackend.lesson.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.tesla.teslabackend.course.entity.Semana;
import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    @JsonBackReference
    @EqualsAndHashCode.Exclude
    private Semana semana;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private Integer orden;

    @OneToMany(mappedBy = "leccion", cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    private Set<Pregunta> preguntas = new LinkedHashSet<>();
}