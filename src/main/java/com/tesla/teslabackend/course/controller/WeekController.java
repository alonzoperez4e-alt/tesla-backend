package com.tesla.teslabackend.course.controller;

import com.tesla.teslabackend.course.entity.Semana;
import com.tesla.teslabackend.course.service.WeekService;
import com.tesla.teslabackend.course.dto.CrearSemanaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/weeks")
public class WeekController {

    @Autowired
    private WeekService weekService;

    // Antes: /api/v1/admin/semanas
    // Ahora: POST /api/v1/weeks
    @PostMapping
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<Semana> crearSemana(@RequestBody CrearSemanaDTO dto) {
        return ResponseEntity.ok(weekService.crearSemana(dto));
    }
}