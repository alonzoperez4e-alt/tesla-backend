package com.tesla.teslabackend.progress.service;

import com.tesla.teslabackend.progress.entity.EstadisticasAlumno;
import com.tesla.teslabackend.progress.entity.HistorialRanking;
import com.tesla.teslabackend.progress.repository.EstadisticasAlumnoRepository;
import com.tesla.teslabackend.progress.repository.HistorialRankingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@Slf4j
public class RankingCronTask {

    @Autowired private EstadisticasAlumnoRepository estadisticasRepository;
    @Autowired private HistorialRankingRepository historialRepository;

    @Scheduled(cron = "0 0 0 * * MON", zone = "America/Lima")
    @Transactional
    public void reiniciarTorneoSemanal() {
        log.info("⏰ Iniciando guardado de Historial y reinicio semanal...");
        try {
            List<EstadisticasAlumno> rankingActual = estadisticasRepository.findAllByOrderByExpSemanalDesc();

            LocalDate fechaFin = LocalDate.now(ZoneId.of("America/Lima"));
            int mes = fechaFin.getMonthValue();
            int anio = fechaFin.getYear();

            int posicionGuardado = 1;
            for (EstadisticasAlumno alumno : rankingActual) {
                // Solo guardamos en el historial si tienen experiencia mayor a 0
                if (alumno.getExpSemanal() != null && alumno.getExpSemanal() > 0) {
                    HistorialRanking registro = HistorialRanking.builder()
                            .usuario(alumno.getUsuario())
                            .expObtenida(alumno.getExpSemanal())
                            .posicion(posicionGuardado)
                            .fechaFinSemana(fechaFin)
                            .mes(mes)
                            .anio(anio)
                            .build();
                    historialRepository.save(registro);
                }
                posicionGuardado++;
            }

            int posicionActual = 1;
            for (EstadisticasAlumno alumno : rankingActual) {
                if (alumno.getExpSemanal() == null || alumno.getExpSemanal() == 0) {
                    alumno.setRankingAnterior(0);
                } else {
                    alumno.setRankingAnterior(posicionActual);
                }

                alumno.setExpSemanal(0);
                posicionActual++;
            }

            estadisticasRepository.saveAll(rankingActual);

            log.info("✅ Historial COMPLETO guardado, ranking anterior actualizado y torneo semanal reiniciado a 0.");

        } catch (Exception e) {
            log.error("❌ Error al reiniciar el ranking semanal: {}", e.getMessage(), e);
        }
    }
}