package com.tesla.teslabackend.dto.creacion;

public record CrearSemanaDTO(
        Integer idCurso,
        Integer nroSemana,
        boolean isBloqueada
) {}