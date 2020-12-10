/*
 * Copyright (c) 2018-2020, RTE (https://www.rte-france.com)
 * Copyright (c) 2019-2020 RTE international (https://www.rte-international.com)
 * See AUTHORS.txt
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 * This file is part of the Let’s Coordinate project.
 */

package org.lfenergy.letscoordinate.backend.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateUtil {

    public static OffsetDateTime toOffsetDateTime(String value) {
        if (value == null)
            return null;
        return Instant.parse(value).atOffset(ZoneOffset.systemDefault().getRules().getOffset(LocalDateTime.now()));
    }

    public static String formatDate(TemporalAccessor dateTime) {
        return formatDate(dateTime, "dd/MM/yyyy");
    }

    public static String formatDate(TemporalAccessor dateTime, String format) {
        return DateTimeFormatter.ofPattern(format).format(dateTime);
    }

    public static ZoneId getParisZoneId() {
        return ZoneId.of("Europe/Paris");
    }

    public static List<LocalDateTime> getDatesForMultiYearView(LocalDate startDate, LocalDate endDate) {
        List<LocalDateTime> dates = new ArrayList<>();
        for (int year = startDate.getYear(); year <= endDate.getYear() ; year++) {
            dates.add(LocalDateTime.of(year, Month.JANUARY, 1, 12, 30));
        }
        return dates;
    }
}
