package com.tesla.teslabackend.progress.service;

import com.tesla.teslabackend.common.util.TimeUtil;
import com.tesla.teslabackend.progress.dto.RankingItemDTO;
import com.tesla.teslabackend.progress.entity.EstadisticasAlumno;
import com.tesla.teslabackend.progress.repository.EstadisticasAlumnoRepository;
import com.tesla.teslabackend.progress.repository.IntentoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * El ranking semanal se calcula directamente contra PostgreSQL. Antes se cacheaba
 * en un ZSET de Redis (ElastiCache), eliminado en la optimizacion FinOps; la
 * consulta agregada ya existia como camino de respaldo y ahora es el unico.
 */
@Service
@Slf4j
public class RankingService {

    private static final int TAMANIO_TOP = 100;

    @Autowired private EstadisticasAlumnoRepository estadisticasRepository;
    @Autowired private IntentoRepository intentoRepository;

    @Transactional(readOnly = true)
    public List<RankingItemDTO> obtenerRanking(Integer idUsuarioLogueado) {
        ZonedDateTime ahora = ZonedDateTime.now(ZoneId.of("America/Lima"));
        ZonedDateTime inicioSemana = TimeUtil.obtenerInicioDeSemana(ahora);
        ZonedDateTime finSemana = inicioSemana.plusWeeks(1);

        List<Object[]> agregado = intentoRepository.findExpAgregadaPorVentana(inicioSemana, finSemana);
        if (agregado.isEmpty()) return new ArrayList<>();

        List<Integer> idsTop = new ArrayList<>();
        Map<Integer, Integer> scoresMap = new HashMap<>();

        for (Object[] fila : agregado.subList(0, Math.min(agregado.size(), TAMANIO_TOP))) {
            Integer idUser = (Integer) fila[0];
            idsTop.add(idUser);
            scoresMap.put(idUser, Math.toIntExact((Long) fila[1]));
        }

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
