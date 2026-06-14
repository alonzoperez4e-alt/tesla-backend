package com.tesla.teslabackend.progress.controller;

import com.tesla.teslabackend.progress.dto.HistorialRankingDTO;
import com.tesla.teslabackend.progress.dto.RankingItemDTO;
import com.tesla.teslabackend.progress.entity.EstadisticasAlumno;
import com.tesla.teslabackend.progress.entity.HistorialRanking;
import com.tesla.teslabackend.progress.repository.EstadisticasAlumnoRepository;
import com.tesla.teslabackend.progress.repository.HistorialRankingRepository;
import com.tesla.teslabackend.progress.service.RankingService;
import com.tesla.teslabackend.user.component.IdentityExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ranking")
@CrossOrigin(origins = "*")
public class RankingController {

    @Autowired
    private HistorialRankingRepository historialRepository;

    @Autowired
    private RankingService rankingService;

    @Autowired
    private IdentityExtractor identityExtractor;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> obtenerRankingGeneral(@AuthenticationPrincipal Jwt jwt) {
        Integer userId = (jwt != null) ? identityExtractor.getUsuarioId(jwt) : null;

        List<RankingItemDTO> rankingDTOs = rankingService.obtenerRanking(userId);
        List<Map<String, Object>> respuesta = new ArrayList<>();

        for (RankingItemDTO dto : rankingDTOs) {
            Map<String, Object> map = new HashMap<>();
            map.put("idUsuario", dto.getIdUsuario());
            map.put("posicion", dto.getPosicionActual());
            map.put("nombreCompleto", dto.getNombreCompleto());

            map.put("expParaRanking", dto.getExpTotal());
            map.put("expTotal", dto.getExpTotal());
            map.put("experiencia", dto.getExpTotal());

            map.put("rankingAnterior", dto.getTendencia() + dto.getPosicionActual());

            map.put("esUsuarioActual", dto.isEsUsuarioActual());
            map.put("inicial", dto.getInicial());
            map.put("tendencia", dto.getTendencia());

            respuesta.add(map);
        }

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/semanal")
    public ResponseEntity<List<Map<String, Object>>> obtenerRankingActual(@AuthenticationPrincipal Jwt jwt) {
        return obtenerRankingGeneral(jwt);
    }

    @GetMapping("/historial")
    public ResponseEntity<List<HistorialRankingDTO>> obtenerHistorial(
            @RequestParam Integer mes,
            @RequestParam Integer anio) {

        List<HistorialRanking> historial = historialRepository.findByMesAndAnio(mes, anio);
        List<HistorialRankingDTO> dtos = historial.stream().map(h -> new HistorialRankingDTO(
                h.getIdHistorial(),
                h.getUsuario().getNombre() + " " + h.getUsuario().getApellido(),
                h.getExpObtenida(),
                h.getPosicion(),
                h.getFechaFinSemana()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/historial/fechas")
    public ResponseEntity<List<LocalDate>> obtenerFechasHistorial() {
        return ResponseEntity.ok(historialRepository.obtenerFechasDisponibles());
    }

    @GetMapping("/historial/admin")
    public ResponseEntity<List<Map<String, Object>>> obtenerRankingPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        List<HistorialRanking> historial = historialRepository.findByFechaFinSemanaOrderByPosicionAsc(fecha);
        List<Map<String, Object>> respuesta = new ArrayList<>();

        for (HistorialRanking h : historial) {
            Map<String, Object> dto = new HashMap<>();
            dto.put("posicion", h.getPosicion());
            dto.put("nombreCompleto", h.getUsuario().getNombre() + " " + h.getUsuario().getApellido());
            dto.put("expParaRanking", h.getExpObtenida());
            respuesta.add(dto);
        }

        return ResponseEntity.ok(respuesta);
    }
}