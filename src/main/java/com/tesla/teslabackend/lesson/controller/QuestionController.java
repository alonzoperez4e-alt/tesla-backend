package com.tesla.teslabackend.lesson.controller;

import com.tesla.teslabackend.lesson.entity.Pregunta;
import com.tesla.teslabackend.lesson.service.QuestionService;
import com.tesla.teslabackend.lesson.dto.CrearPreguntaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @PreAuthorize("hasRole('administrador')")
    public ResponseEntity<Pregunta> crearPregunta(
            @RequestPart("pregunta") CrearPreguntaDTO dto) {

        return ResponseEntity.ok(questionService.crearPreguntaConAlternativas(dto));
    }
}