package com.tesla.teslabackend.controller;

import com.tesla.teslabackend.dto.CaminoCursoDTO;
import com.tesla.teslabackend.entity.Curso;
import com.tesla.teslabackend.service.AprendizajeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estudiantes")
// @CrossOrigin eliminado para evitar conflictos con CorsConfig
public class EstudianteController {

    @Autowired
    private AprendizajeService aprendizajeService;

    @GetMapping("/cursos")
    public ResponseEntity<List<Curso>> listarCursos() {
        return ResponseEntity.ok(aprendizajeService.obtenerCursosDisponibles());
    }

    @GetMapping("/curso/{cursoId}/camino")
    public ResponseEntity<CaminoCursoDTO> verCaminoCurso(
            @PathVariable Integer cursoId,
            @RequestParam Integer usuarioId) {
        return ResponseEntity.ok(aprendizajeService.obtenerCaminoDelCurso(cursoId, usuarioId));
    }
}