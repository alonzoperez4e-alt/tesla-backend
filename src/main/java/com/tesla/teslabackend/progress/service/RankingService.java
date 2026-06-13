package com.tesla.teslabackend.progress.service;

import com.tesla.teslabackend.common.util.TimeUtil;
import com.tesla.teslabackend.progress.dto.RankingItemDTO;
import com.tesla.teslabackend.progress.entity.EstadisticasAlumno;
import com.tesla.teslabackend.progress.repository.EstadisticasAlumnoRepository;
import com.tesla.teslabackend.progress.repository.IntentoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RankingService {

    @Autowired private EstadisticasAlumnoRepository estadisticasRepository;
    @Autowired private StringRedisTemplate redisTemplate;
    @Autowired private RankingRedisService rankingRedisService;
    @Autowired private IntentoRepository intentoRepository;

    @Transactional(readOnly = true)
    public List<RankingItemDTO> obtenerRanking(Integer idUsuarioLogueado) {
        ZonedDateTime ahora = ZonedDateTime.now(ZoneId.of("America/Lima"));
        String clave = "ranking:sem:" + TimeUtil.semanaISOdeLima(ahora);

        Set<ZSetOperations.TypedTuple<String>> rankingRedis = null;
        boolean redisDisponible = true;

        try {
            if (Boolean.FALSE.equals(redisTemplate.hasKey(clave))) {
                ZonedDateTime inicioSemana = TimeUtil.obtenerInicioDeSemana(ahora);
                ZonedDateTime finSemana = inicioSemana.plusWeeks(1);
                rankingRedisService.reconstruirDesdeAurora(clave, inicioSemana, finSemana);
            }
            rankingRedis = redisTemplate.opsForZSet().reverseRangeWithScores(clave, 0, 99);
        } catch (Exception e) {
            log.warn("Redis no está disponible. Fallback a Aurora para el ranking. Error: {}", e.getMessage());
            redisDisponible = false;
        }

        List<Integer> idsTop = new ArrayList<>();
        Map<Integer, Integer> scoresMap = new HashMap<>();

        if (redisDisponible && rankingRedis != null && !rankingRedis.isEmpty()) {
            for (ZSetOperations.TypedTuple<String> tuple : rankingRedis) {
                Integer idUser = Integer.valueOf(Objects.requireNonNull(tuple.getValue()));
                if (idUser == -1) continue;

                Double score = tuple.getScore();
                int finalScore = score != null ? score.intValue() : 0;

                idsTop.add(idUser);
                scoresMap.put(idUser, finalScore);
            }
        } else if (!redisDisponible) {
            ZonedDateTime inicioSemana = TimeUtil.obtenerInicioDeSemana(ahora);
            ZonedDateTime finSemana = inicioSemana.plusWeeks(1);
            List<Object[]> dbRanking = intentoRepository.findExpAgregadaPorVentana(inicioSemana, finSemana);

            for (int i = 0; i < Math.min(dbRanking.size(), 100); i++) {
                Integer idUser = (Integer) dbRanking.get(i)[0];
                Integer finalScore = Math.toIntExact((Long) dbRanking.get(i)[1]);
                idsTop.add(idUser);
                scoresMap.put(idUser, finalScore);
            }
        }

        if (idsTop.isEmpty()) return new ArrayList<>();

        List<EstadisticasAlumno> statsList = estadisticasRepository.findAllById(idsTop);
        Map<Integer, EstadisticasAlumno> statsMap = statsList.stream()
                .collect(Collectors.toMap(s -> s.getUsuario().getIdUsuario(), s -> s));

        List<RankingItemDTO> rankingDTOs = new ArrayList<>();
        int posicion = 1;

        for (Integer idUser : idsTop) {
            EstadisticasAlumno stats = statsMap.get(idUser);
            if (stats == null) continue;

            String nombre = stats.getUsuario().getNombre();
            String apellido = stats.getUsuario().getApellido();
            String nombreCompleto = (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
            String inicial = (nombre != null && !nombre.isEmpty()) ? nombre.substring(0, 1).toUpperCase() : "?";

            int rankAnt = (stats.getRankingAnterior() != null && stats.getRankingAnterior() > 0)
                    ? stats.getRankingAnterior() : posicion;
            int tendencia = rankAnt - posicion;
            boolean esYo = idUser.equals(idUsuarioLogueado);

            RankingItemDTO dto = new RankingItemDTO(
                    idUser,
                    nombreCompleto.trim(),
                    inicial,
                    scoresMap.get(idUser),
                    posicion,
                    tendencia,
                    esYo
            );

            rankingDTOs.add(dto);
            posicion++;
        }

        return rankingDTOs;
    }
}