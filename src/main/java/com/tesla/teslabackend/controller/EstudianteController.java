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
@CrossOrigin(origins = "*")
public class EstudianteController {

    @Autowired
    private AprendizajeService aprendizajeService;

    // 1. Obtener cursos habilitados(Selector del dashboard)
    @GetMapping("/cursos")
    public ResponseEntity<List<Curso>> listarCursos() {
        return ResponseEntity.ok(aprendizajeService.obtenerCursosDisponibles());
    }

    // 2. Obtener el camino de aprendizaje (Semanas y lecciones)
    // URL ejemplo: /api/estudiantes/curso/1/camino?usuarioId=5
    @GetMapping("/curso/{cursoId}/camino")
    public ResponseEntity<CaminoCursoDTO> verCaminoCurso(
            @PathVariable Integer cursoId,
            @RequestParam Integer usuarioId) {

        return ResponseEntity.ok(aprendizajeService.obtenerCaminoDelCurso(cursoId, usuarioId));
    }
}