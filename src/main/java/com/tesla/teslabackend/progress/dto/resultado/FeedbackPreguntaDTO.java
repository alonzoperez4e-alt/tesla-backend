package com.tesla.teslabackend.progress.dto.resultado;

public record FeedbackPreguntaDTO(
        Integer idPregunta,
        boolean correcta,
        Integer idAlternativaCorrecta,
        String solucionTexto,
        String solucionImagenUrl
) {}