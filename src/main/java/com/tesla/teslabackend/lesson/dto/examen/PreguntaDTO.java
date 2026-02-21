package com.tesla.teslabackend.lesson.dto.examen;

import java.util.List;

public record PreguntaDTO(
        Integer idPregunta,
        String textoPregunta,
        List<AlternativaDTO> alternativas
) {}