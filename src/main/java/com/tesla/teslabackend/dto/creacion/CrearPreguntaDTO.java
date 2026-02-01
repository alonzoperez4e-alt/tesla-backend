package com.tesla.teslabackend.dto.creacion;

import java.util.List;

public record CrearPreguntaDTO(
        Integer idLeccion,
        String textoPregunta,
        String solucionTexto,
        String solucionImagenUrl,
        List<CrearAlternativaDTO> alternativas
) {}