package org.lfenergy.letscoordinate.backend.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateUtilTest {

    public static String formatDate(TemporalAccessor dateTime) {
        return formatDate(dateTime, "dd/MM/yyyy");
    }

    public static String formatDate(TemporalAccessor dateTime, String format) {
        return DateTimeFormatter.ofPattern(format).format(dateTime);
    }

    public static ZoneId getParisZoneId() {
        return ZoneId.of("Europe/Paris");
    }
}
