package com.tesla.teslabackend.progress.dto.resultado;

import java.util.List;

public record ResultadoEvaluacionDTO(
        Integer puntajeObtenido,
        Integer expGanada,
        boolean leccionAprobada,
        List<FeedbackPreguntaDTO> feedback
) {}