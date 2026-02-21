package com.tesla.teslabackend.lesson.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alternativa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alternativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alternativa")
    private Integer idAlternativa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta")
    @JsonBackReference
    private Pregunta pregunta;

    @Column(name = "texto_alternativa", nullable = false, columnDefinition = "TEXT")
    private String textoAlternativa;

    @Column(name = "is_correcta")
    @Builder.Default
    private Boolean isCorrecta = false;
}
