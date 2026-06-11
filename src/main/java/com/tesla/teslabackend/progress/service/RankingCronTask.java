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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

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

            // [inicio, fin) de la semana que acaba de cerrar
            ZonedDateTime fin = ahora.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .toLocalDate().atStartOfDay(zonaLima);
            ZonedDateTime inicio = fin.minusWeeks(1);

            LocalDate fechaFinSemana = fin.minusDays(1).toLocalDate(); // Domingo que cerró
            int mes = fechaFinSemana.getMonthValue();
            int anio = fechaFinSemana.getYear();

            // 1. Agregación desde Aurora (Verdad Durable)
            List<Object[]> rankingAgregado = intentoRepository.findExpAgregadaPorVentana(inicio, fin);

            int posicionActual = 1;
            for (Object[] fila : rankingAgregado) {
                Integer idUsuario = (Integer) fila[0];
                Long expGanadaLong = (Long) fila[1];
                Integer expObtenida = expGanadaLong.intValue();

                EstadisticasAlumno stats = estadisticasRepository.findById(idUsuario).orElse(null);
                if (stats == null) continue;

                // 2. UPSERT Idempotente en HistorialRanking (Constraint uq_historial protege de duplicados)
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

                // 3. Actualizar rankingAnterior
                stats.setRankingAnterior(posicionActual);
                estadisticasRepository.save(stats);

                posicionActual++;
            }

            // A los que no puntuaron esta semana, su rankingAnterior vuelve a 0
            List<EstadisticasAlumno> todos = estadisticasRepository.findAll();
            for (EstadisticasAlumno stats : todos) {
                boolean puntuoEstaSemana = rankingAgregado.stream()
                        .anyMatch(fila -> fila[0].equals(stats.getUsuario().getIdUsuario()));

                if (!puntuoEstaSemana && (stats.getRankingAnterior() == null || stats.getRankingAnterior() != 0)) {
                    stats.setRankingAnterior(0);
                    estadisticasRepository.save(stats);
                }
            }

            // ¡IMPORTANTE! Ya no reseteamos stats.setExpSemanal(0)
            log.info("✅ Snapshot semanal materializado de forma segura. Top actual: {} usuarios.", rankingAgregado.size());

        } catch (Exception e) {
            log.error("Error al materializar el snapshot semanal: {}", e.getMessage(), e);
        }
    }
}