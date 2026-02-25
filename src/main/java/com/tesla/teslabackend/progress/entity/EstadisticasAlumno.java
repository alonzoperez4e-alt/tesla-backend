package com.tesla.teslabackend.progress.entity;

import com.tesla.teslabackend.user.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "estadisticas_alumno")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasAlumno {

    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_usuario", nullable = false)
    @ToString.Exclude
    private Usuario usuario;

    @Column(name = "racha_actual")
    private Integer rachaActual;

    @Column(name = "exp_total")
    private Integer expTotal;

    @Column(name = "exp_semanal")
    @Builder.Default
    private Integer expSemanal = 0;

    @Column(name = "estado_mascota")
    private String estadoMascota;

    @Column(name = "ultima_fecha_mision")
    private LocalDate ultimaFechaMision;

    @Column(name = "ranking_anterior")
    private Integer rankingAnterior;

    public void verificarYReiniciarRacha() {
        if (this.ultimaFechaMision != null) {
            LocalDate hoy = LocalDate.now();
            long diasDiferencia = ChronoUnit.DAYS.between(this.ultimaFechaMision, hoy);

            if (diasDiferencia > 1) {
                this.rachaActual = 0;
            }
        }
    }

    public void calcularEstado() {
        int exp = (this.expTotal != null) ? this.expTotal : 0;
        if (exp < 1250) this.estadoMascota = "Huevo";
        else if (exp < 2500) this.estadoMascota = "Agrietándose";
        else if (exp < 3750) this.estadoMascota = "Naciendo";
        else this.estadoMascota = "Completamente Crecido";
    }
}