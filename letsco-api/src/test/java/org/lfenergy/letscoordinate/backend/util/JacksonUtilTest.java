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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
public class JacksonUtilTest {

    InstantWrapperForTest instantWrapperForTest;
    OffsetDateTimeWrapperForTest offsetDateTimeWrapperForTest;

    @BeforeEach
    void init() {
        instantWrapperForTest = InstantWrapperForTest.builder()
                .instant(Instant.parse("2021-03-03T14:57:00Z"))
                .build();
        offsetDateTimeWrapperForTest = OffsetDateTimeWrapperForTest.builder()
                .offsetDateTime(OffsetDateTime.of(2021, 5, 5, 21, 13, 41, 0, ZoneOffset.UTC))
                .build();
    }

    @Test
    void testGeneratedSerializeMapper() throws JsonProcessingException {
        ObjectMapper objectMapper = JacksonUtil.generateSerializeMapper();

        String instantWrapperAsString = objectMapper.writeValueAsString(instantWrapperForTest);
        assertEquals("{\"instant\":\"2021-03-03T14:57:00Z\"}", instantWrapperAsString);

        String offsetDateTimeWrapperAsString = objectMapper.writeValueAsString(offsetDateTimeWrapperForTest);
        assertEquals("{\"offsetDateTime\":\"2021-05-05T21:13:41Z\"}", offsetDateTimeWrapperAsString);
    }

    @Test
    void testGeneratedDeserializeMapper() throws JsonProcessingException {
        ObjectMapper objectMapper = JacksonUtil.generateDeserializeMapper();

        InstantWrapperForTest instantWrapper = objectMapper.readValue("{\"instant\":\"2021-03-03T14:57:00Z\"}", InstantWrapperForTest.class);
        assertEquals(Instant.parse("2021-03-03T14:57:00Z"), instantWrapper.getInstant());

        OffsetDateTimeWrapperForTest offsetDateTimeWrapper = objectMapper.readValue("{\"offsetDateTime\":\"2021-05-05T21:13:41Z\"}", OffsetDateTimeWrapperForTest.class);
        assertEquals(OffsetDateTime.of(2021, 5, 5, 21, 13, 41, 0, ZoneOffset.UTC), offsetDateTimeWrapper.getOffsetDateTime());
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class InstantWrapperForTest {
        private Instant instant;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class OffsetDateTimeWrapperForTest {
        private OffsetDateTime offsetDateTime;
    }

}
