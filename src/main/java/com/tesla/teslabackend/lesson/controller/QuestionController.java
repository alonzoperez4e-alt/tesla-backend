package com.tesla.teslabackend.lesson.controller;

import com.tesla.teslabackend.lesson.entity.Pregunta;
import com.tesla.teslabackend.lesson.service.QuestionService;
import com.tesla.teslabackend.lesson.dto.CrearPreguntaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    // Antes: /api/v1/admin/preguntas
    // Ahora: POST /api/v1/questions
    @PostMapping
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<Pregunta> crearPregunta(@RequestBody CrearPreguntaDTO dto) {
        return ResponseEntity.ok(questionService.crearPreguntaConAlternativas(dto));
    }
}