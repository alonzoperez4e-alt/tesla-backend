package com.tesla.teslabackend.controller;

import com.tesla.teslabackend.dto.ranking.RankingItemDTO;
import com.tesla.teslabackend.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/ranking")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    @GetMapping
    public ResponseEntity<List<RankingItemDTO>> getRanking(@RequestParam Integer userId) {
        return ResponseEntity.ok(rankingService.obtenerRanking(userId));
    }
}
