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

import io.vavr.control.Validation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.config.LetscoProperties;
import org.lfenergy.letscoordinate.backend.dto.ResponseErrorDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageWrapperDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.*;
import org.lfenergy.letscoordinate.backend.util.JsonUtils;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class EventMessageServiceTest {

    EventMessageService eventMessageService;
    @MockBean
    CoordinationConfig coordinationConfig;
    LetscoProperties letscoProperties;

    @BeforeEach
    public void before() {
        letscoProperties = new LetscoProperties();
        LetscoProperties.InputFile.Validation validation = new LetscoProperties.InputFile.Validation();
        LetscoProperties.InputFile inputFile = new LetscoProperties.InputFile();
        inputFile.setValidation(validation);
        letscoProperties.setInputFile(inputFile);
        eventMessageService = new EventMessageService(coordinationConfig, letscoProperties);
    }

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
    public void validateEventMessageDto_nullInput() {
        Validation<ResponseErrorDto, EventMessageDto> validation = eventMessageService.validateEventMessageDto(null);
        assertAll(
                () -> assertNotNull(validation),
                () -> assertTrue(validation.isInvalid()),
                () -> assertEquals(HttpStatus.BAD_REQUEST.value(), validation.getError().getStatus()),
                () -> assertEquals("INVALID_INPUT_FILE", validation.getError().getCode()),
                () -> assertEquals(1, validation.getError().getMessages().size())
        );
    }

    @Test
    public void validateEventMessageDto_emptyInput() {
        EventMessageDto eventMessageDto = new EventMessageDto();
        Validation<ResponseErrorDto, EventMessageDto> validation = eventMessageService.validateEventMessageDto(eventMessageDto);
        assertAll(
                () -> assertNotNull(validation),
                () -> assertTrue(validation.isInvalid()),
                () -> assertEquals(HttpStatus.BAD_REQUEST.value(), validation.getError().getStatus()),
                () -> assertEquals("INVALID_INPUT_FILE", validation.getError().getCode()),
                () -> assertEquals(2, validation.getError().getMessages().size())
        );
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

    @Test
    public void getMissingMandatoryFields_businessDayFromNull() throws IOException {
        EventMessageDto eventMessageDto =
                JsonUtils.jsonToObject("ProcessSuccessful.json", EventMessageWrapperDto.class).getEventMessage();
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setBusinessDayFrom(null);
        Set<String> missingMandatoryFields = eventMessageService.getMissingMandatoryFields(eventMessageDto);
        assertEquals(1, missingMandatoryFields.size());
        assertTrue(missingMandatoryFields.stream().anyMatch(
                f -> f.equals("header.properties.businessDataIdentifier.businessDayFrom")));
    }

    @Test
    public void getMissingMandatoryFields_businessDayFromNull_businessDayFromIsOptional() throws IOException {
        EventMessageDto eventMessageDto =
                JsonUtils.jsonToObject("ProcessSuccessful.json", EventMessageWrapperDto.class).getEventMessage();
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setBusinessDayFrom(null);
        letscoProperties.getInputFile().getValidation().setBusinessDayFromOptional(true);
        Set<String> missingMandatoryFields = eventMessageService.getMissingMandatoryFields(eventMessageDto);
        assertEquals(0, missingMandatoryFields.size());
        assertEquals(eventMessageDto.getHeader().getTimestamp(),
                eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getBusinessDayFrom());
    }

    @Test
    public void getMissingMandatoryFields_validationBusinessTimestampsNull() throws IOException {
        EventMessageDto eventMessageDto = JsonUtils.jsonToObject("MessageValidated_NEGATIVE_ACK.json",
                EventMessageWrapperDto.class).getEventMessage();
        eventMessageDto.getPayload().getValidation().get().getValidationMessages().get().forEach(
                vm -> vm.setBusinessTimestamp(null));
        Set<String> missingMandatoryFields = eventMessageService.getMissingMandatoryFields(eventMessageDto);
        assertEquals(4, missingMandatoryFields.stream().filter(
                f -> f.matches("payload\\.validation\\.validationMessages\\[[0-9]+\\]\\.businessTimestamp")).count());
    }

    @Test
    public void getMissingMandatoryFields_validationBusinessTimestampsNull_validationBusinessTimestampIsOptional()
            throws IOException {
        EventMessageDto eventMessageDto = JsonUtils.jsonToObject("MessageValidated_NEGATIVE_ACK.json",
                EventMessageWrapperDto.class).getEventMessage();
        letscoProperties.getInputFile().getValidation().setValidationBusinessTimestampOptional(true);
        eventMessageDto.getPayload().getValidation().get().getValidationMessages().get().forEach(
                vm -> vm.setBusinessTimestamp(null));
        Set<String> missingMandatoryFields = eventMessageService.getMissingMandatoryFields(eventMessageDto);
        assertEquals(0, missingMandatoryFields.size());
    }
}
