package com.tesla.teslabackend.progress.service;

import com.tesla.teslabackend.progress.entity.EstadisticasAlumno;
import com.tesla.teslabackend.progress.entity.HistorialRanking;
import com.tesla.teslabackend.progress.repository.EstadisticasAlumnoRepository;
import com.tesla.teslabackend.progress.repository.HistorialRankingRepository;
import com.tesla.teslabackend.progress.repository.IntentoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RankingCronTask {

    @Autowired private EstadisticasAlumnoRepository estadisticasRepository;
    @Autowired private HistorialRankingRepository historialRepository;
    @Autowired private IntentoRepository intentoRepository;

    @Scheduled(cron = "0 0 0 * * MON", zone = "America/Lima")
    @Transactional
    public void materializarSnapshotSemanal() {
        try {
            ZoneId zonaLima = ZoneId.of("America/Lima");
            ZonedDateTime ahora = ZonedDateTime.now(zonaLima);

            ZonedDateTime fin = ahora.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .toLocalDate().atStartOfDay(zonaLima);
            ZonedDateTime inicio = fin.minusWeeks(1);

            LocalDate fechaFinSemana = fin.minusDays(1).toLocalDate();
            int mes = fechaFinSemana.getMonthValue();
            int anio = fechaFinSemana.getYear();

            List<Object[]> rankingAgregado = intentoRepository.findExpAgregadaPorVentana(inicio, fin);

            Set<Integer> idsQuePuntuaron = rankingAgregado.stream()
                    .map(f -> (Integer) f[0])
                    .collect(Collectors.toSet());

            Map<Integer, EstadisticasAlumno> statsMap = estadisticasRepository.findAllById(idsQuePuntuaron)
                    .stream().collect(Collectors.toMap(s -> s.getUsuario().getIdUsuario(), s -> s));

            int posicionActual = 1;
            for (Object[] fila : rankingAgregado) {
                Integer idUsuario = (Integer) fila[0];
                // SOLUCIÓN: Math.toIntExact evita truncamientos silenciosos
                Integer expObtenida = Math.toIntExact((Long) fila[1]);

                EstadisticasAlumno stats = statsMap.get(idUsuario);
                if (stats == null) continue;

                HistorialRanking registro = historialRepository
                        .findByFechaFinSemanaAndUsuarioIdUsuario(fechaFinSemana, idUsuario)
                        .orElseGet(() -> HistorialRanking.builder()
                                .usuario(stats.getUsuario())
                                .fechaFinSemana(fechaFinSemana)
                                .mes(mes)
                                .anio(anio)
                                .build());

                registro.setExpObtenida(expObtenida);
                registro.setPosicion(posicionActual);
                historialRepository.save(registro);

                stats.setRankingAnterior(posicionActual);
                posicionActual++;
            }

            List<EstadisticasAlumno> todos = estadisticasRepository.findAll();
            for (EstadisticasAlumno stats : todos) {
                boolean puntuoEstaSemana = idsQuePuntuaron.contains(stats.getIdUsuario());

                if (!puntuoEstaSemana && (stats.getRankingAnterior() == null || stats.getRankingAnterior() != 0)) {
                    stats.setRankingAnterior(0);
                }
            }

            estadisticasRepository.saveAll(todos);

            log.info("Snapshot semanal materializado de forma segura. Top actual: {} usuarios.", rankingAgregado.size());

        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("Error CRÍTICO al materializar el snapshot semanal. Transacción revertida.", e);
        }
    }
}