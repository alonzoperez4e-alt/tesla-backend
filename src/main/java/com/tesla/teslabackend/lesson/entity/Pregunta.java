package com.tesla.teslabackend.lesson.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "pregunta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pregunta")
    private Integer idPregunta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_leccion")
    @JsonBackReference
    @EqualsAndHashCode.Exclude
    private Leccion leccion;

    @Column(name = "texto_pregunta", nullable = false, columnDefinition = "TEXT")
    private String textoPregunta;

    @Column(name = "pregunta_imagen_key", columnDefinition = "TEXT")
    private String preguntaImagenKey;

    @Column(name = "solucion_texto", columnDefinition = "TEXT")
    private String solucionTexto;

    @Column(name = "solucion_imagen_key", columnDefinition = "TEXT")
    private String solucionImagenKey;

    @Builder.Default
    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    private Set<Alternativa> alternativas = new LinkedHashSet<>();
}