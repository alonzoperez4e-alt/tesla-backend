package com.tesla.teslabackend.lesson.controller;

import com.tesla.teslabackend.lesson.dto.detalle.SemanaDetalleDTO;
import com.tesla.teslabackend.progress.dto.SolicitudCalificacionDTO;
import com.tesla.teslabackend.lesson.dto.examen.CuestionarioDTO;
import com.tesla.teslabackend.progress.dto.resultado.ResultadoEvaluacionDTO;
import com.tesla.teslabackend.lesson.dto.CrearLeccionDTO;
import com.tesla.teslabackend.lesson.entity.Leccion;
import com.tesla.teslabackend.lesson.service.LessonService;
import com.tesla.teslabackend.progress.service.EvaluacionService; // Importamos el servicio de progreso
import com.tesla.teslabackend.user.component.IdentityExtractor;
import com.tesla.teslabackend.user.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lessons")
public class LessonController {

    @Autowired private LessonService lessonService;
    @Autowired private EvaluacionService evaluacionService;
    @Autowired private IdentityExtractor identityExtractor;

    // Antes: /api/v1/admin/lecciones
    // Ahora: POST /api/v1/lessons
    @PostMapping
    @PreAuthorize("hasRole('administrador')")
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
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SolicitudCalificacionDTO solicitud) { // <-- Pro-Tip de Seguridad JWT

        Usuario usuario = identityExtractor.getUsuario(jwt);

        return ResponseEntity.ok(evaluacionService.calificarLeccion(idLeccion, solicitud, usuario));
    }

    @GetMapping("/{idSemana}/detalle")
    @PreAuthorize("hasRole('administrador')")
    public ResponseEntity<SemanaDetalleDTO> verDetalleSemana(
            @PathVariable Integer idSemana){
        return ResponseEntity.ok(lessonService.verDetalle(idSemana));
    }

    @DeleteMapping("/{idLeccion}")
    public ResponseEntity<Void> eliminarLeccion(@PathVariable Integer idLeccion) {
        lessonService.eliminarLeccion(idLeccion);
        return ResponseEntity.noContent().build();
    }

}