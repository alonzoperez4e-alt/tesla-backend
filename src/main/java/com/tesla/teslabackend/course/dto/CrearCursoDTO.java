package com.tesla.teslabackend.course.dto;

public record CrearCursoDTO(
        String nombre,
        String descripcion,
        boolean isHabilitado
) {}