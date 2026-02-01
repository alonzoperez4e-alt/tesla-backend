package com.tesla.teslabackend.dto.creacion;

public record CrearCursoDTO(
        String nombre,
        String descripcion,
        boolean isHabilitado
) {}