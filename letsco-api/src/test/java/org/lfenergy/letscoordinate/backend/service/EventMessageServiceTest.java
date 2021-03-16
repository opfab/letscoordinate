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

package org.lfenergy.letscoordinate.backend.service;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.*;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
public class EventMessageServiceTest {

    @InjectMocks
    EventMessageService eventMessageService;

    @Spy
    CoordinationConfig coordinationConfig = null;

    private EventMessageDto initEventMessageDto() {
        EventMessageDto eventMessageDto = new EventMessageDto();

        eventMessageDto.getPayload().setText(Collections.singletonList(new TextDataDto()));

        eventMessageDto.getPayload().setLinks(Collections.singletonList(new LinkDataDto()));

        RscKpiTemporalDataDto rscKpiTemporalDataDto = new RscKpiTemporalDataDto();
        RscKpiDataDetailsDto rscKpiDataDetailsDto = new RscKpiDataDetailsDto();
        rscKpiDataDetailsDto.setDetail(Collections.singletonList(rscKpiTemporalDataDto));
        RscKpiDataDto rscKpiDataDto = new RscKpiDataDto();
        rscKpiDataDto.setData(Collections.singletonList(rscKpiDataDetailsDto));
        eventMessageDto.getPayload().setRscKpi(Collections.singletonList(rscKpiDataDto));

        TimeserieTemporalDataDto timeserieTemporalDataDto = new TimeserieTemporalDataDto();
        TimeserieDataDetailsDto timeserieDataDetailsDto = new TimeserieDataDetailsDto();
        timeserieDataDetailsDto.setDetail(Collections.singletonList(timeserieTemporalDataDto));
        TimeserieDataDto timeserieDataDto = new TimeserieDataDto();
        timeserieDataDto.setData(Collections.singletonList(timeserieDataDetailsDto));
        eventMessageDto.getPayload().setTimeserie(Collections.singletonList(timeserieDataDto));

        ValidationMessageDto validationMessageDto = new ValidationMessageDto();
        ValidationDto validationDto = new ValidationDto();
        validationDto.setValidationMessages(Collections.singletonList(validationMessageDto));
        eventMessageDto.getPayload().setValidation(validationDto);

        return eventMessageDto;
    }

    @Test
    public void getMissingMandatoryFields_shouldReturnAllMissingFields() {

        EventMessageDto eventMessageDto = initEventMessageDto();

        Set<String> validationResultSet = eventMessageService.getMissingMandatoryFields(eventMessageDto);
        Set<String> expectedSet = Stream.of(
                "xmlns",
                "header.verb",
                "header.noun",
                "header.timestamp",
                "header.source",
                "header.messageId",
                "header.properties.format",
                "header.properties.businessDataIdentifier.messageTypeName",
                "header.properties.businessDataIdentifier.businessDayFrom",
                "payload.text[0].name",
                "payload.text[0].value",
                "payload.links[0].name",
                "payload.links[0].value",
                "payload.rscKpi[0].name",
                "payload.rscKpi[0].data[0].timestamp",
                "payload.rscKpi[0].data[0].granularity",
                "payload.rscKpi[0].data[0].detail[0].value",
                "payload.timeserie[0].name",
                "payload.timeserie[0].data[0].timestamp",
                "payload.timeserie[0].data[0].detail[0].value",
                "payload.validation.status",
                "payload.validation.result",
                "payload.validation.validationType",
                "payload.validation.validationMessages[0].code",
                "payload.validation.validationMessages[0].title",
                "payload.validation.validationMessages[0].businessTimestamp",
                "payload.validation.validationMessages[0].severity",
                "payload.validation.validationMessages[0].message"
        ).collect(Collectors.toSet());
        Assertions.assertThat(validationResultSet.size()).isEqualTo(expectedSet.size());
        Assertions.assertThat(validationResultSet).containsAll(expectedSet);

        eventMessageDto.getHeader().getProperties().setBusinessDataIdentifier(null);
        eventMessageDto.getPayload().getRscKpi().get(0).getData().get(0).setDetail(null);
        eventMessageDto.getPayload().getTimeserie().get(0).getData().get(0).setDetail(null);
        eventMessageDto.getPayload().getValidation().get().setValidationMessages(null);

        validationResultSet = eventMessageService.getMissingMandatoryFields(eventMessageDto);
        expectedSet = Stream.of(
                "xmlns",
                "header.verb",
                "header.noun",
                "header.timestamp",
                "header.source",
                "header.messageId",
                "header.properties.format",
                "header.properties.businessDataIdentifier",
                "payload.text[0].name",
                "payload.text[0].value",
                "payload.links[0].name",
                "payload.links[0].value",
                "payload.rscKpi[0].name",
                "payload.rscKpi[0].data[0].timestamp",
                "payload.rscKpi[0].data[0].granularity",
                "payload.rscKpi[0].data[0].detail",
                "payload.timeserie[0].name",
                "payload.timeserie[0].data[0].timestamp",
                "payload.timeserie[0].data[0].detail",
                "payload.validation.status",
                "payload.validation.result",
                "payload.validation.validationType",
                "payload.validation.validationMessages"
        ).collect(Collectors.toSet());
        Assertions.assertThat(validationResultSet.size()).isEqualTo(expectedSet.size());
        Assertions.assertThat(validationResultSet).containsAll(expectedSet);

        eventMessageDto.getHeader().setProperties(null);
        eventMessageDto.getPayload().getRscKpi().get(0).setData(null);
        eventMessageDto.getPayload().getTimeserie().get(0).setData(null);

        validationResultSet = eventMessageService.getMissingMandatoryFields(eventMessageDto);
        expectedSet = Stream.of(
                "xmlns",
                "header.verb",
                "header.noun",
                "header.timestamp",
                "header.source",
                "header.messageId",
                "header.properties",
                "payload.text[0].name",
                "payload.text[0].value",
                "payload.links[0].name",
                "payload.links[0].value",
                "payload.rscKpi[0].name",
                "payload.rscKpi[0].data",
                "payload.timeserie[0].name",
                "payload.timeserie[0].data",
                "payload.validation.status",
                "payload.validation.result",
                "payload.validation.validationType",
                "payload.validation.validationMessages"
        ).collect(Collectors.toSet());
        Assertions.assertThat(validationResultSet.size()).isEqualTo(expectedSet.size());
        Assertions.assertThat(validationResultSet).containsAll(expectedSet);

        eventMessageDto.setHeader(null);
        eventMessageDto.setPayload(null);

        validationResultSet = eventMessageService.getMissingMandatoryFields(eventMessageDto);
        expectedSet = Stream.of("xmlns", "header", "payload").collect(Collectors.toSet());
        Assertions.assertThat(validationResultSet.size()).isEqualTo(expectedSet.size());
        Assertions.assertThat(validationResultSet).containsAll(expectedSet);
    }

}
