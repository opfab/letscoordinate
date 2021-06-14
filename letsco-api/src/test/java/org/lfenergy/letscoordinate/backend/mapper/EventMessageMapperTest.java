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

package org.lfenergy.letscoordinate.backend.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.CoordinationCommentDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.TimeserieDataDetailsDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.TimeserieDataDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.TimeserieOutputResultDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.TimeserieTemporalDataDto;
import org.lfenergy.letscoordinate.backend.enums.CoordinationStatusEnum;
import org.lfenergy.letscoordinate.backend.enums.OutputResultAnswerEnum;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.lfenergy.letscoordinate.backend.util.Constants.*;

@ExtendWith(MockitoExtension.class)
public class EventMessageMapperTest {

    EventMessageDto initEventMessageDto() {
        EventMessageDto eventMessageDto = new EventMessageDto();

        eventMessageDto.getHeader().setTimestamp(Instant.parse("2021-05-31T05:13:00Z"));
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setBusinessDayFrom(Instant.parse("2021-05-31T00:00:00Z"));
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setBusinessDayTo(Instant.parse("2021-05-31T23:59:59Z"));
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setCaseId("caseIdTest001");
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setCoordinationStatus(CoordinationStatusEnum.CON);
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setCoordinationComments(Arrays.asList(
                CoordinationCommentDto.builder()
                        .eicCode("22XCORESO------S")
                        .generalComment("Super!")
                        .build()
        ));

        TimeserieTemporalDataDto timeserieTemporalDataDto_Event = new TimeserieTemporalDataDto();
        timeserieTemporalDataDto_Event.setLabel(EVENT_KEY);
        timeserieTemporalDataDto_Event.setValue("Event A");
        TimeserieTemporalDataDto timeserieTemporalDataDto_Constraint = new TimeserieTemporalDataDto();
        timeserieTemporalDataDto_Constraint.setLabel(CONSTRAINT_KEY);
        timeserieTemporalDataDto_Constraint.setValue("Constraint A");
        TimeserieTemporalDataDto timeserieTemporalDataDto_RemedialActions = new TimeserieTemporalDataDto();
        timeserieTemporalDataDto_RemedialActions.setLabel(REMEDIAL_ACTIONS_KEY);
        timeserieTemporalDataDto_RemedialActions.setValue("RemedialActions A");
        timeserieTemporalDataDto_RemedialActions.setResults(Arrays.asList(
                TimeserieOutputResultDto.builder()
                        .eicCode("10XFR-RTE------Q")
                        .answer(OutputResultAnswerEnum.CON)
                        .explanation("Explanation 1")
                        .comment("Not ok!")
                        .build()
        ));
        TimeserieDataDetailsDto timeserieDataDetailsDto = new TimeserieDataDetailsDto();
        timeserieDataDetailsDto.setTimestamp(OffsetDateTime.of(2021, 5, 31, 0, 0, 0, 0, ZoneOffset.UTC));
        timeserieDataDetailsDto.setDetail(Arrays.asList(
                timeserieTemporalDataDto_Event,
                timeserieTemporalDataDto_Constraint,
                timeserieTemporalDataDto_RemedialActions
        ));
        TimeserieDataDto timeserieDataDto = new TimeserieDataDto();
        timeserieDataDto.setData(Collections.singletonList(timeserieDataDetailsDto));
        eventMessageDto.getPayload().setTimeserie(Collections.singletonList(timeserieDataDto));

        return eventMessageDto;
    }

    @Test
    public void fromDto_nullDtoParam(){
        assertNull(EventMessageMapper.fromDto(null));
    }

    @Test
    public void fromDto_notNullDtoParamWithValidOutputValues(){
        assertNotNull(EventMessageMapper.fromDto(initEventMessageDto()));
    }

}
