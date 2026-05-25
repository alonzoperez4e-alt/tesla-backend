package com.tesla.teslabackend.group.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "grupo_miembros")
@Data
@NoArgsConstructor
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "student_id", nullable = false, unique = true)
    private Long studentId; // Un estudiante solo puede estar en un grupo a la vez

    @Column(name = "group_exp")
    private Integer groupExp = 0; // EXP independiente para el grupo

    @Column(name = "joined_at", updatable = false)
    private ZonedDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        this.joinedAt = ZonedDateTime.now(ZoneId.of("America/Lima"));
    }
}