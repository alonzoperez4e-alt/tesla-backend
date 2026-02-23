package com.tesla.teslabackend.lesson.controller;

import com.tesla.teslabackend.progress.dto.SolicitudCalificacionDTO;
import com.tesla.teslabackend.lesson.dto.examen.CuestionarioDTO;
import com.tesla.teslabackend.progress.dto.resultado.ResultadoEvaluacionDTO;
import com.tesla.teslabackend.lesson.dto.CrearLeccionDTO;
import com.tesla.teslabackend.lesson.entity.Leccion;
import com.tesla.teslabackend.lesson.service.LessonService;
import com.tesla.teslabackend.progress.service.EvaluacionService; // Importamos el servicio de progreso
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lessons")
public class LessonController {

    @Autowired private LessonService lessonService;
    @Autowired private EvaluacionService evaluacionService;

    // Antes: /api/v1/admin/lecciones
    // Ahora: POST /api/v1/lessons
    @PostMapping
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<Leccion> crearLeccion(@RequestBody CrearLeccionDTO dto) {
        return ResponseEntity.ok(lessonService.crearLeccion(dto));
    }

    // ==========================================
    // ENDPOINTS DE ESTUDIANTES (Tus nuevos métodos)
    // ==========================================

    // 1. Obtener preguntas (GET) - Al abrir el modal
    @GetMapping("/{idLeccion}/quiz")
    public ResponseEntity<CuestionarioDTO> obtenerContenidoLeccion(@PathVariable Integer idLeccion) {
        return ResponseEntity.ok(lessonService.obtenerCuestionario(idLeccion));
    }

    // 2. Enviar respuestas y calificar (POST) - Al dar click en "Terminar"
    @PostMapping("/{idLeccion}/submit")
    public ResponseEntity<ResultadoEvaluacionDTO> finalizarLeccion(
            @PathVariable Integer idLeccion,
            @RequestBody SolicitudCalificacionDTO solicitud,
            Authentication authentication) { // <-- Pro-Tip de Seguridad JWT

        // Extraemos el ID real del usuario desde su token, nadie puede falsificar esto
        Integer usuarioIdAutenticado = Integer.parseInt(authentication.getName());

        return ResponseEntity.ok(evaluacionService.calificarLeccion(idLeccion, usuarioIdAutenticado, solicitud));
    }
}