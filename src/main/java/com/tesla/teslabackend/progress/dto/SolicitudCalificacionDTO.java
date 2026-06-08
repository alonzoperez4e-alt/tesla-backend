package com.tesla.teslabackend.progress.dto;

import java.util.List;

public record SolicitudCalificacionDTO(
        List<RespuestaAlumnoDTO> respuestas
) {}