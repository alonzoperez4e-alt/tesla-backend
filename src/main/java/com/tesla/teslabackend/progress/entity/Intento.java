package com.tesla.teslabackend.progress.entity;

import com.tesla.teslabackend.user.entity.Usuario;
import com.tesla.teslabackend.lesson.entity.Leccion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "intento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Intento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_intento")
    private Integer idIntento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    @ToString.Exclude
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_leccion")
    @ToString.Exclude
    private Leccion leccion;

    private Integer puntaje;

    @Column(name = "is_primer_intento")
    @Builder.Default
    private Boolean isPrimerIntento = true;

    @CreationTimestamp
    private LocalDateTime fecha;
}