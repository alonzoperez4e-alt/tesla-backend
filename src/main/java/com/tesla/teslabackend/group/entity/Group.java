package com.tesla.teslabackend.group.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "grupos")
@Data
@NoArgsConstructor
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 10)
    private String code; // Código para invitar (ej. TSL-A1B2)

    @Column(name = "creator_id", nullable = false)
    private Long creatorId; // ID del estudiante creador

    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        // Aseguramos que la fecha se guarde con la zona horaria de Trujillo, Perú
        this.createdAt = ZonedDateTime.now(ZoneId.of("America/Lima"));
    }
}