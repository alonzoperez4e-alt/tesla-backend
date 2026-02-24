package com.tesla.teslabackend.progress.service;

import com.tesla.teslabackend.progress.repository.EstadisticasAlumnoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RankingCronTask {

    private static final Logger log = LoggerFactory.getLogger(RankingCronTask.class);

    @Autowired
    private EstadisticasAlumnoRepository estadisticasRepository;

    // Se ejecuta a las 00:00:00 cada Lunes en Perú (America/Lima)
    @Scheduled(cron = "0 0 0 * * MON", zone = "America/Lima")
    @Transactional
    public void reiniciarTorneoSemanal() {
        log.info("⏰ [ACADEMIA TESLA] Iniciando reinicio del Ranking Semanal...");
        try {
            estadisticasRepository.reiniciarExperienciaSemanal();
            log.info("✅ Ranking semanal reiniciado a 0 exitosamente.");
        } catch (Exception e) {
            log.error("❌ Error al reiniciar el ranking semanal: {}", e.getMessage());
        }
    }
}