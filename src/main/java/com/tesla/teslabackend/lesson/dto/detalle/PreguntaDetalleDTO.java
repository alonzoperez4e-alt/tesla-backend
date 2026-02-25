package com.tesla.teslabackend.lesson.dto.detalle;


import java.util.List;

public record PreguntaDetalleDTO(
        Integer idPregunta,
        String textoPregunta,
        String preguntaImagenUrl,
        String textoSolucion,
        String solucionImagenUrl,
        List<AlternativaDetalleDTO> alternativas) {
}
