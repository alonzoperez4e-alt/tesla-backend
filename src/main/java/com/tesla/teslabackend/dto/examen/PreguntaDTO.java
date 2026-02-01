package com.tesla.teslabackend.dto.examen;

import java.util.List;

public record PreguntaDTO(
        Integer idPregunta,
        String textoPregunta,
        List<AlternativaDTO> alternativas
) {}