package com.tesla.teslabackend.lesson.dto.examen;

import java.util.List;

public record CuestionarioDTO(
        Integer idLeccion,
        String nombreLeccion,
        List<PreguntaDTO> preguntas
) {}