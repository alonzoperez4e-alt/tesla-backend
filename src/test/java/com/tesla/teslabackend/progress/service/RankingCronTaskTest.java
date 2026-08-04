package com.tesla.teslabackend.progress.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.support.CronExpression;

import com.tesla.teslabackend.progress.repository.EstadisticasAlumnoRepository;
import com.tesla.teslabackend.progress.repository.HistorialRankingRepository;
import com.tesla.teslabackend.progress.repository.IntentoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * El snapshot semanal solo se materializa si la tarea ECS esta viva, y la tarea
 * solo vive entre las 18:00 y las 24:00. Estas pruebas fijan las dos cosas que
 * pueden romperse en silencio: que el placeholder del cron resuelve (un error de
 * sintaxis reventaria al arrancar en produccion, no aqui) y que el horario por
 * defecto sigue cayendo dentro de la ventana de servicio.
 */
class RankingCronTaskTest {

    /** Mismo valor que el default del placeholder en {@code RankingCronTask}. */
    private static final String CRON_POR_DEFECTO = "0 5 18 * * MON";

    private static final int HORA_APERTURA = 18;
    private static final int HORA_CIERRE = 24;

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(EstadisticasAlumnoRepository.class, () -> mock(EstadisticasAlumnoRepository.class))
            .withBean(HistorialRankingRepository.class, () -> mock(HistorialRankingRepository.class))
            .withBean(IntentoRepository.class, () -> mock(IntentoRepository.class))
            .withUserConfiguration(SchedulingConfig.class, RankingCronTask.class);

    /** Activa el procesado de {@code @Scheduled} para que el cron se resuelva de verdad. */
    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    static class SchedulingConfig {
    }

    @Test
    void seRegistraYElCronSeResuelve_conLaConfiguracionPorDefecto() {
        runner.run(context -> assertThat(context)
                .hasNotFailed()
                .hasSingleBean(RankingCronTask.class));
    }

    @Test
    void seRespetaElCronConfigurado() {
        runner.withPropertyValues("app.ranking.snapshot.cron=0 30 19 * * MON")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void elCronPorDefectoCaeDentroDeLaVentanaDeServicio() {
        LocalDateTime siguiente = CronExpression.parse(CRON_POR_DEFECTO)
                .next(LocalDateTime.of(2026, 1, 1, 0, 0));

        assertThat(siguiente).isNotNull();
        assertThat(siguiente.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(siguiente.getHour())
                .as("el snapshot debe ejecutarse con el servicio encendido (%d:00-%d:00)", HORA_APERTURA, HORA_CIERRE)
                .isBetween(HORA_APERTURA, HORA_CIERRE - 1);
    }
}
