package com.tesla.teslabackend.lesson.dto.detalle;

import java.util.List;

public record SemanaDetalleDTO(
        Integer idSemana,
        Integer nroSemana,
        boolean isBloqueada, // Viene directo de la decisión del Admin
        List<LeccionDetalleDTO> lecciones) {

}
