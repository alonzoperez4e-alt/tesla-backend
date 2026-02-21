package com.tesla.teslabackend.progress.controller;

import com.tesla.teslabackend.progress.dto.RankingItemDTO;
import com.tesla.teslabackend.progress.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ranking")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    @GetMapping
    public ResponseEntity<List<RankingItemDTO>> getRanking(Authentication authentication) {
        // Obtenemos el ID del usuario directamente del Token de seguridad
        Integer idUsuarioLogueado = Integer.parseInt(authentication.getName());

        return ResponseEntity.ok(rankingService.obtenerRanking(idUsuarioLogueado));
    }
}