package com.tesla.teslabackend.course.dto;

import java.util.List;

// DTO Principal que agrupa todo el camino
public record CaminoCursoDTO(
        Integer idCurso,
        String nombreCurso,
        List<SemanaDTO> semanas
) {}