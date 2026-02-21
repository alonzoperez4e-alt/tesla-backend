package com.tesla.teslabackend.course.dto;

import java.util.List;

public record SemanaDTO(
        Integer idSemana,
        Integer nroSemana,
        boolean isBloqueada, // Viene directo de la decisión del Admin
        List<LeccionDTO> lecciones
) {}