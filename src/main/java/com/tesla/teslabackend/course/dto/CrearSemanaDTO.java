package com.tesla.teslabackend.course.dto;

public record CrearSemanaDTO(
        Integer idCurso,
        Integer nroSemana,
        boolean isBloqueada
) {}