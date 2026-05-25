package com.tesla.teslabackend.course.dto;

public record ViewLeccionDTO(
        Integer idLeccion,
        String nombre,
        Integer orden
) {}