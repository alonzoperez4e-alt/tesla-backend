package com.tesla.teslabackend.dto.examen;

import java.util.List;

public record CuestionarioDTO(
        Integer idLeccion,
        String nombreLeccion,
        List<PreguntaDTO> preguntas
) {}