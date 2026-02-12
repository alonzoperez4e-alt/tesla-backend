package com.tesla.teslabackend.entity;

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

    @OneToOne
    @MapsId // Esto dice: "Mi ID es el mismo que el del Usuario asociado"
    @JoinColumn(name = "id_usuario")
    @ToString.Exclude
    private Usuario usuario;

    @Column(name = "racha_actual")
    private Integer rachaActual;

    @Column(name = "exp_total")
    private Integer expTotal;

    @Column(name = "estado_mascota")
    private String estadoMascota;

    @Column(name = "ultima_fecha_mision")
    private LocalDate ultimaFechaMision;

    // Este método se llama desde el Service cuando obtiene los datos
    public void verificarYReiniciarRacha() {
        if (this.ultimaFechaMision != null) {
            LocalDate hoy = LocalDate.now();
            long diasDiferencia = ChronoUnit.DAYS.between(this.ultimaFechaMision, hoy);

            // Si pasó más de 1 día sin actividad, la racha vuelve a 0 automáticamente
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