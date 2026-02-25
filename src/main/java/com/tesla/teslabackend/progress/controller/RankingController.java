package com.tesla.teslabackend.progress.controller;

import com.tesla.teslabackend.progress.dto.HistorialRankingDTO;
import com.tesla.teslabackend.progress.entity.EstadisticasAlumno;
import com.tesla.teslabackend.progress.entity.HistorialRanking;
import com.tesla.teslabackend.progress.repository.EstadisticasAlumnoRepository;
import com.tesla.teslabackend.progress.repository.HistorialRankingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private EstadisticasAlumnoRepository estadisticasRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> obtenerRankingGeneral(@RequestParam(required = false) Integer userId) {
        List<EstadisticasAlumno> todosLosAlumnos = estadisticasRepository.findAllByOrderByExpSemanalDesc();

        List<Map<String, Object>> respuesta = new ArrayList<>();
        int posicion = 1;

        for (EstadisticasAlumno alumno : todosLosAlumnos) {
            Map<String, Object> dto = new HashMap<>();
            dto.put("idUsuario", alumno.getUsuario().getIdUsuario());
            dto.put("posicion", posicion);
            dto.put("nombreCompleto", alumno.getUsuario().getNombre() + " " + alumno.getUsuario().getApellido());

            int expSemanal = (alumno.getExpSemanal() != null) ? alumno.getExpSemanal() : 0;
            dto.put("expParaRanking", expSemanal);
            dto.put("expTotal", expSemanal);
            dto.put("experiencia", expSemanal);

            // ✨ ENVIAMOS EL RANKING ANTERIOR
            int rankAnt = (alumno.getRankingAnterior() != null) ? alumno.getRankingAnterior() : 0;
            dto.put("rankingAnterior", rankAnt);

            boolean esUsuarioActual = (userId != null && alumno.getUsuario().getIdUsuario().equals(userId));
            dto.put("esUsuarioActual", esUsuarioActual);

            respuesta.add(dto);
            posicion++;
        }

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/semanal")
    public ResponseEntity<List<Map<String, Object>>> obtenerRankingActual(@RequestParam(required = false) Integer userId) {
        List<EstadisticasAlumno> rankingActual = estadisticasRepository.findAllByOrderByExpSemanalDesc();
        List<Map<String, Object>> respuesta = new ArrayList<>();
        int posicion = 1;

        for (EstadisticasAlumno alumno : rankingActual) {
            if (alumno.getExpSemanal() != null && alumno.getExpSemanal() > 0) {
                Map<String, Object> dto = new HashMap<>();
                dto.put("idUsuario", alumno.getUsuario().getIdUsuario());
                dto.put("posicion", posicion);
                dto.put("nombreCompleto", alumno.getUsuario().getNombre() + " " + alumno.getUsuario().getApellido());
                dto.put("expParaRanking", alumno.getExpSemanal());

                int rankAnt = (alumno.getRankingAnterior() != null) ? alumno.getRankingAnterior() : 0;
                dto.put("rankingAnterior", rankAnt);

                boolean esUsuarioActual = (userId != null && alumno.getUsuario().getIdUsuario().equals(userId));
                dto.put("esUsuarioActual", esUsuarioActual);

                respuesta.add(dto);
                posicion++;
            }
        }
        return ResponseEntity.ok(respuesta);
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
}