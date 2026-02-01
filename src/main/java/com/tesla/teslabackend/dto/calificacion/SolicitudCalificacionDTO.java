package com.tesla.teslabackend.dto.calificacion;

import java.util.List;

public record SolicitudCalificacionDTO(
        Integer idUsuario,
        List<RespuestaAlumnoDTO> respuestas
) {}