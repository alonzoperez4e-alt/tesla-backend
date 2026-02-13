package com.tesla.teslabackend.controller;

import com.tesla.teslabackend.dto.creacion.*;
import com.tesla.teslabackend.entity.*;
import com.tesla.teslabackend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // 1. Crear Curso
    @PostMapping("/cursos")
    public ResponseEntity<Curso> crearCurso(@RequestBody CrearCursoDTO dto) {
        return ResponseEntity.ok(adminService.crearCurso(dto));
    }

    // 2. Crear Semana (Asignada a un curso)
    @PostMapping("/semanas")
    public ResponseEntity<Semana> crearSemana(@RequestBody CrearSemanaDTO dto) {
        return ResponseEntity.ok(adminService.crearSemana(dto));
    }

    // 3. Crear Lección (Asignada a una semana)
    @PostMapping("/lecciones")
    public ResponseEntity<Leccion> crearLeccion(@RequestBody CrearLeccionDTO dto) {
        return ResponseEntity.ok(adminService.crearLeccion(dto));
    }

    // 4. Crear Pregunta con Alternativas (Asignada a una lección)
    @PostMapping("/preguntas")
    public ResponseEntity<Pregunta> crearPregunta(@RequestBody CrearPreguntaDTO dto) {
        return ResponseEntity.ok(adminService.crearPreguntaConAlternativas(dto));
    }
}