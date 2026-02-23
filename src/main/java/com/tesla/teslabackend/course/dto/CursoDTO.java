package com.tesla.teslabackend.course.dto;

public record CursoDTO(
        Integer idCurso,
        String nombre,
        String descripcion,
        Boolean isHabilitado
) {
}
