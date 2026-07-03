package com.tesla.teslabackend.progress.controller;

import com.tesla.teslabackend.progress.entity.EstadisticasAlumno;
import com.tesla.teslabackend.progress.service.EstadisticaService;
import com.tesla.teslabackend.user.component.IdentityExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/stats")
public class EstadisticaController {

    @Autowired
    private EstadisticaService service;

    @Autowired
    private IdentityExtractor identityExtractor;

    @GetMapping("/me")
    public ResponseEntity<EstadisticasAlumno> getStats(@AuthenticationPrincipal Jwt jwt) {
        Integer idUsuario = identityExtractor.getUsuarioId(jwt);
        return ResponseEntity.ok(service.obtenerPorId(idUsuario));
    }

    @PostMapping("/mision-completa")
    public ResponseEntity<EstadisticasAlumno> completarMision(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, Integer> payload) {

        Integer idUsuario = identityExtractor.getUsuarioId(jwt);

        // Suponiendo que el front envía {"exp": 50}
        int expGanada = payload.get("exp");
        return ResponseEntity.ok(service.actualizarProgreso(idUsuario, expGanada));
    }
}