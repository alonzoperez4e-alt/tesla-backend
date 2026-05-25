package com.tesla.teslabackend.lesson.dto;

public record CrearLeccionDTO(
        Integer idSemana,
        String nombre,
        String descripcion,
        Integer orden
) {}