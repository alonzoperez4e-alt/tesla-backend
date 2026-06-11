package com.tesla.teslabackend.common.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.IsoFields;

public class TimeUtil {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    
    public static String semanaISOdeLima(ZonedDateTime fecha) {
        ZonedDateTime limaTime = fecha.withZoneSameInstant(ZONA_LIMA);
        int year = limaTime.get(IsoFields.WEEK_BASED_YEAR);
        int week = limaTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);

        return String.format("%d-W%02d", year, week);
    }

    public static ZonedDateTime obtenerInicioDeSemana(ZonedDateTime fecha) {
        return fecha.withZoneSameInstant(ZONA_LIMA)
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .toLocalDate().atStartOfDay(ZONA_LIMA);
    }
}