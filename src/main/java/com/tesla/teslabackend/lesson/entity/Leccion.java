package com.tesla.teslabackend.lesson.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.tesla.teslabackend.course.entity.Semana;
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
    @JsonBackReference
    private Semana semana;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private Integer orden;

    @OneToMany(mappedBy = "leccion", cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonManagedReference
    private List<Pregunta> preguntas;
}