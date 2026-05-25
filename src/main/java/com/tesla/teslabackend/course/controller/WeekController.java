package com.tesla.teslabackend.course.controller;

import com.tesla.teslabackend.course.dto.ViewSemanaDTO;
import com.tesla.teslabackend.course.entity.Semana;
import com.tesla.teslabackend.course.service.WeekService;
import com.tesla.teslabackend.course.dto.CrearSemanaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/weeks")
public class WeekController {

    @Autowired
    private WeekService weekService;

    // Antes: /api/v1/admin/semanas
    // Ahora: POST /api/v1/weeks
    @PostMapping("/{cursoId}")
    @PreAuthorize("hasRole('administrador')")
    public ResponseEntity<Semana> crearSemana(
            @PathVariable Integer cursoId,
            @RequestBody CrearSemanaDTO dto) {
        return ResponseEntity.ok(weekService.crearSemana(cursoId, dto));
    }

    @GetMapping("/{cursoId}")
    public ResponseEntity<List<ViewSemanaDTO>> listarSemanas(
            @PathVariable Integer cursoId) {
        return ResponseEntity.ok(weekService.obtenerSemanas(cursoId));
    }

    @DeleteMapping("/{semanaId}")
    public ResponseEntity<Void> eliminarSemana(@PathVariable Integer semanaId) {
        weekService.eliminarSemana(semanaId);
        return ResponseEntity.noContent().build();

    }
}