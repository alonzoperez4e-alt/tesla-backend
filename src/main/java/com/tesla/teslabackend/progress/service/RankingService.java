package com.tesla.teslabackend.progress.service;

import com.tesla.teslabackend.common.util.TimeUtil;
import com.tesla.teslabackend.progress.dto.RankingItemDTO;
import com.tesla.teslabackend.progress.entity.EstadisticasAlumno;
import com.tesla.teslabackend.progress.repository.EstadisticasAlumnoRepository;
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
public class RankingService {

    @Autowired private EstadisticasAlumnoRepository estadisticasRepository;
    @Autowired private StringRedisTemplate redisTemplate;
    @Autowired private RankingRedisService rankingRedisService;

    @Transactional(readOnly = true)
    public List<RankingItemDTO> obtenerRanking(Integer idUsuarioLogueado) {
        ZonedDateTime ahora = ZonedDateTime.now(ZoneId.of("America/Lima"));
        String clave = "ranking:sem:" + TimeUtil.semanaISOdeLima(ahora);

        // reconstruir desde Aurora si Redis está vacío (falla de caché o inicio de semana limpia)
        if (Boolean.FALSE.equals(redisTemplate.hasKey(clave))) {
            ZonedDateTime inicioSemana = TimeUtil.obtenerInicioDeSemana(ahora);
            ZonedDateTime finSemana = inicioSemana.plusWeeks(1);
            rankingRedisService.reconstruirDesdeAurora(clave, inicioSemana, finSemana);
        }

        // obtener ids ordenados desde redis (del mayor score al menor)
        Set<ZSetOperations.TypedTuple<String>> rankingRedis = redisTemplate.opsForZSet().reverseRangeWithScores(clave, 0, 99);

        if (rankingRedis == null || rankingRedis.isEmpty()) return new ArrayList<>();

        List<Integer> idsTop = rankingRedis.stream()
                .map(tuple -> Integer.valueOf(Objects.requireNonNull(tuple.getValue())))
                .collect(Collectors.toList());

        // consultar la informacion pesada a BD (Nombres, rankingAnterior)
        List<EstadisticasAlumno> statsList = estadisticasRepository.findAllById(idsTop);
Map<Integer, EstadisticasAlumno> statsMap = statsList.stream()
        .collect(Collectors.toMap(EstadisticasAlumno::getIdUsuario, s -> s));

        List<RankingItemDTO> rankingDTOs = new ArrayList<>();
        int posicion = 1;

        // armar dtos basado en el orden dado por Redis
        for (ZSetOperations.TypedTuple<String> tuple : rankingRedis) {
            Integer idUser = Integer.valueOf(Objects.requireNonNull(tuple.getValue()));
            Double score = tuple.getScore();
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

            assert score != null;
            RankingItemDTO dto = new RankingItemDTO(
                    idUser,
                    nombreCompleto.trim(),
                    inicial,
                    score.intValue(),
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