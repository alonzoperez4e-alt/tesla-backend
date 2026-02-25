package com.tesla.teslabackend.lesson.dto.detalle;

public record AlternativaDetalleDTO(
        Integer idAlternativa,
        String texto,
        boolean isCorrecta
) {
}
