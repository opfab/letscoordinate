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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

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

    public static boolean isValidJsonDate(String dateStr) {
        try {
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(dateStr);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
