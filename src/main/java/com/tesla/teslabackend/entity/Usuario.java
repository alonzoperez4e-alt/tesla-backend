package com.tesla.teslabackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "codigo_usuario", unique = true, nullable = false, length = 20)
    private String codigoUsuario;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String password;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Rol rol;

    @Column(length = 50)
    private String area;

    @CreationTimestamp
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    // Relación con Estadisticas (1 a 1)
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private EstadisticasAlumno estadisticas;

    // Relación con Intentos (1 a N)
    // Excluimos del ToString para evitar bucles infinitos
    @ToString.Exclude
    @OneToMany(mappedBy = "usuario")
    private List<Intento> intentos;
}