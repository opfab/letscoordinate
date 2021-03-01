/*
 * Copyright (c) 2020-2021, RTE (https://www.rte-france.com)
 * Copyright (c) 2020-2021 RTE international (https://www.rte-international.com)
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
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    public void isValidJsonDate_shouldReturnFalse(){
        assertThat(DateUtil.isValidJsonDate(null)).isFalse();
        assertThat(DateUtil.isValidJsonDate("")).isFalse();
        assertThat(DateUtil.isValidJsonDate("2021-01-01T00:00:00.000Z")).isFalse();
        assertThat(DateUtil.isValidJsonDate("999-01-01T00:00:00Z")).isFalse();
        assertThat(DateUtil.isValidJsonDate("2021-1-01T00:00:00Z")).isFalse();
        assertThat(DateUtil.isValidJsonDate("2021-99-01T00:00:00Z")).isFalse();
        assertThat(DateUtil.isValidJsonDate("2021-01-1T00:00:00Z")).isFalse();
        assertThat(DateUtil.isValidJsonDate("2021-01-99T00:00:00Z")).isFalse();
        assertThat(DateUtil.isValidJsonDate("2021-01-01T0:00:00Z")).isFalse();
        assertThat(DateUtil.isValidJsonDate("2021-01-01T99:00:00Z")).isFalse();
        assertThat(DateUtil.isValidJsonDate("2021-01-01T00:0:00Z")).isFalse();
        assertThat(DateUtil.isValidJsonDate("2021-01-01T00:99:00Z")).isFalse();
        assertThat(DateUtil.isValidJsonDate("2021-01-01T00:00:0Z")).isFalse();
        assertThat(DateUtil.isValidJsonDate("2021-01-01T00:00:99Z")).isFalse();
    }

    @Test
    public void isValidJsonDate_shouldReturnTrue() {
        assertThat(DateUtil.isValidJsonDate("2021-01-01T00:00:00Z")).isTrue();
        assertThat(DateUtil.isValidJsonDate("9999-12-31T23:59:59Z")).isTrue();
    }
}
