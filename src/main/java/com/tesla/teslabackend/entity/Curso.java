package com.tesla.teslabackend.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "curso")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_curso")
    private Integer idCurso;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "is_habilitado")
    @Builder.Default
    private Boolean isHabilitado = false;

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonManagedReference
    private List<Semana> semanas;
}
