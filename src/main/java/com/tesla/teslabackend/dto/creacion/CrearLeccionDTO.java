package com.tesla.teslabackend.dto.creacion;

public record CrearLeccionDTO(
        Integer idSemana,
        String nombre,
        String descripcion,
        Integer orden
) {}