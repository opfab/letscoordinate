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
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.TimeserieDataDetailsDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.TimeserieDataDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.TimeserieTemporalDataDto;
import org.lfenergy.letscoordinate.backend.enums.CoordinationAnswerEnum;
import org.lfenergy.letscoordinate.backend.enums.DataGranularityEnum;
import org.lfenergy.letscoordinate.backend.model.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;

import static org.lfenergy.letscoordinate.backend.util.Constants.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CoordinationFactory {

    public static EventMessageDto initEventMessageDto() {
        EventMessageDto eventMessageDto = new EventMessageDto();

        eventMessageDto.getHeader().setTimestamp(Instant.parse("2021-05-31T05:13:00Z"));
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setBusinessDayFrom(Instant.parse("2021-05-31T00:00:00Z"));
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setBusinessDayTo(Instant.parse("2021-05-31T23:59:59Z"));

        TimeserieTemporalDataDto timeserieTemporalDataDto_Event = new TimeserieTemporalDataDto();
        timeserieTemporalDataDto_Event.setLabel(EVENT_KEY);
        timeserieTemporalDataDto_Event.setValue("Event A");
        TimeserieTemporalDataDto timeserieTemporalDataDto_Constraint = new TimeserieTemporalDataDto();
        timeserieTemporalDataDto_Constraint.setLabel(CONSTRAINT_KEY);
        timeserieTemporalDataDto_Constraint.setValue("Constraint A");
        TimeserieTemporalDataDto timeserieTemporalDataDto_RemedialActions = new TimeserieTemporalDataDto();
        timeserieTemporalDataDto_RemedialActions.setLabel(REMEDIAL_ACTIONS_KEY);
        timeserieTemporalDataDto_RemedialActions.setValue("RemedialActions A");
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

    public static EventMessage initEventMessage() {
        return EventMessage.builder()
                .source("Service A")
                .messageTypeName("Coordination A")
                .businessApplication("PanEuropeanServiceATool")
                .businessDayFrom(Instant.parse("2021-05-31T00:00:00Z"))
                .businessDayTo(Instant.parse("2021-05-31T23:59:59Z"))
                .sendingUser("22XCORESO------S")
                .eventMessageRecipients(Arrays.asList(
                        EventMessageRecipient.builder()
                                .eicCode("10XFR-RTE------Q")
                                .build()
                ))
                .build();
    }

    public static Coordination initCoordination() {
        Coordination coordination = new Coordination();
        coordination.setId(1L);
        coordination.setEventMessage(initEventMessage());
        coordination.setProcessKey("coordinationProcessKey");
        coordination.setPublishDate(Instant.parse("2021-05-31T05:13:00Z"));
        coordination.setStartDate(Instant.parse("2021-05-31T00:00:00Z"));
        coordination.setEndDate(Instant.parse("2021-05-31T23:59:59Z"));
        coordination.setStatus(null);
        coordination.setCoordinationRas(Arrays.asList(
                CoordinationRa.builder()
                        .id(2L)
                        .event("Event A")
                        .constraintt("Constraint A")
                        .remedialAction("RemedialActions A")
                        .coordinationRaAnswers(Arrays.asList(
                                CoordinationRaAnswer.builder()
                                        .id(3L)
                                        .eicCode("10XFR-RTE------Q")
                                        .answer(CoordinationAnswerEnum.NOK)
                                        .explanation("Explanation 1")
                                        .comment("Not ok!")
                                        .build()
                        ))
                        .build()
        ));
        return coordination;
    }

}
