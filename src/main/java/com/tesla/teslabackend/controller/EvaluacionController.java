package com.tesla.teslabackend.controller;

import com.tesla.teslabackend.dto.calificacion.*;
import com.tesla.teslabackend.dto.examen.*;
import com.tesla.teslabackend.dto.resultado.*;
import com.tesla.teslabackend.service.EvaluacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lecciones")

public class EvaluacionController {

    @Autowired
    private EvaluacionService evaluacionService;

    // 1. Obtener preguntas (GET) - Al abrir el modal
    @GetMapping("/{idLeccion}/contenido")
    public ResponseEntity<CuestionarioDTO> obtenerContenidoLeccion(@PathVariable Integer idLeccion) {
        return ResponseEntity.ok(evaluacionService.obtenerCuestionario(idLeccion));
    }

    // 2. Enviar respuestas y calificar (POST) - Al dar click en "Terminar"
    @PostMapping("/{idLeccion}/finalizar")
    public ResponseEntity<ResultadoEvaluacionDTO> finalizarLeccion(
            @PathVariable Integer idLeccion,
            @RequestBody SolicitudCalificacionDTO solicitud) {

        return ResponseEntity.ok(evaluacionService.calificarLeccion(idLeccion, solicitud));
    }
}