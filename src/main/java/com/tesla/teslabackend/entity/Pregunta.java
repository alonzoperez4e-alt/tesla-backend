package com.tesla.teslabackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

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
    @ToString.Exclude
    private Leccion leccion;

    @Column(name = "texto_pregunta", nullable = false, columnDefinition = "TEXT")
    private String textoPregunta;

    @Column(name = "solucion_texto", columnDefinition = "TEXT")
    private String solucionTexto;

    @Column(name = "solucion_imagen_url", columnDefinition = "TEXT")
    private String solucionImagenUrl;

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Alternativa> alternativas;
}