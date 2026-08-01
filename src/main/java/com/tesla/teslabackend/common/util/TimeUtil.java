package com.tesla.teslabackend.common.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeUtil {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    public static ZonedDateTime obtenerInicioDeSemana(ZonedDateTime fecha) {
        return fecha.withZoneSameInstant(ZONA_LIMA)
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .toLocalDate().atStartOfDay(ZONA_LIMA);
    }
}