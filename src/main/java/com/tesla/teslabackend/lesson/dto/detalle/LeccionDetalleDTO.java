package com.tesla.teslabackend.lesson.dto.detalle;

import com.tesla.teslabackend.lesson.dto.examen.PreguntaDTO;

import java.util.List;

public record LeccionDetalleDTO(
        Integer idLeccion,
        String nombre,
        Integer orden,
        List<PreguntaDetalleDTO> preguntas
) {
}
