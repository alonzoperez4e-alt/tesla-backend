package com.tesla.teslabackend.controller;

import com.tesla.teslabackend.entity.EstadisticasAlumno;
import com.tesla.teslabackend.service.EstadisticaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/stats")
public class EstadisticaController {

    @Autowired
    private EstadisticaService service;

    // Endpoint para ver el dinosaurio
    @GetMapping("/{idUsuario}")
    public ResponseEntity<EstadisticasAlumno> getStats(@PathVariable Integer idUsuario) {
        System.out.println(">>> ENTRO A STATS <<<");
        return ResponseEntity.ok(service.obtenerPorId(idUsuario));
    }

    // Endpoint para cuando el alumno completa algo (ej. gana 50 EXP)
    @PostMapping("/{idUsuario}/mision-completa")
    public ResponseEntity<EstadisticasAlumno> completarMision(
            @PathVariable Integer idUsuario,
            @RequestBody Map<String, Integer> payload) {

        // Suponiendo que el front envía {"exp": 50}
        int expGanada = payload.get("exp");
        return ResponseEntity.ok(service.actualizarProgreso(idUsuario, expGanada));
    }
}