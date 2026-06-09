package com.tesla.teslabackend.course.controller;

import com.tesla.teslabackend.course.dto.CursoDTO;
import com.tesla.teslabackend.course.entity.Curso;
import com.tesla.teslabackend.course.service.CourseService;
import com.tesla.teslabackend.course.dto.CaminoCursoDTO;
import com.tesla.teslabackend.course.dto.CrearCursoDTO;
import com.tesla.teslabackend.user.component.IdentityExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses") // Ruta unificada para todo lo relacionado a cursos
public class CourseController {

    @Autowired
    private CourseService courseService;

    private IdentityExtractor indentityExtractor;

    // ==========================================
    // ENDPOINTS DE ADMINISTRACIÓN (Protegidos)
    // ==========================================

    @PostMapping
    @PreAuthorize("hasRole('administrador')")
    public ResponseEntity<Curso> crearCurso(@RequestBody CrearCursoDTO dto) {
        return ResponseEntity.ok(courseService.crearCurso(dto));
    }

    // ==========================================
    // ENDPOINTS DE ESTUDIANTES / GENERALES
    // ==========================================

    // Antes: GET /api/v1/estudiantes/cursos
    // Ahora: GET /api/v1/courses
    @GetMapping
    public ResponseEntity<List<CursoDTO>> listarCursos() {
        return ResponseEntity.ok(courseService.obtenerCursosDisponibles());
    }

    // Antes: GET /api/v1/estudiantes/curso/{cursoId}/camino
    // Ahora: GET /api/v1/courses/{cursoId}/path
    @GetMapping("/{cursoId}/path")
    public ResponseEntity<CaminoCursoDTO> verCaminoCurso(
            @PathVariable Integer cursoId,
            @AuthenticationPrincipal Jwt auth) {
        Integer usuarioId = indentityExtractor.getUsuarioId(auth);
        return ResponseEntity.ok(courseService.obtenerCaminoDelCurso(cursoId, usuarioId));
    }
}