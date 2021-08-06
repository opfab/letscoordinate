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

package org.lfenergy.letscoordinate.backend.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vavr.control.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.backend.component.OpfabPublisherComponent;
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.config.LetscoProperties;
import org.lfenergy.letscoordinate.backend.config.OpfabConfig;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.BusinessDataIdentifierDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.HeaderDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.PropertiesDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.PayloadDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.ValidationDto;
import org.lfenergy.letscoordinate.backend.enums.BasicGenericNounEnum;
import org.lfenergy.letscoordinate.backend.enums.ValidationSeverityEnum;
import org.lfenergy.letscoordinate.backend.enums.ValidationTypeEnum;
import org.lfenergy.letscoordinate.backend.exception.IgnoreProcessException;
import org.lfenergy.letscoordinate.backend.exception.JsonDataMandatoryFieldNullException;
import org.lfenergy.letscoordinate.backend.exception.PositiveTechnicalQualityCheckException;
import org.lfenergy.letscoordinate.backend.mapper.EventMessageMapper;
import org.lfenergy.letscoordinate.backend.model.EventMessage;
import org.lfenergy.letscoordinate.backend.processor.ExcelDataProcessor;
import org.lfenergy.letscoordinate.backend.processor.JsonDataProcessor;
import org.lfenergy.letscoordinate.backend.repository.EventMessageRepository;
import org.lfenergy.letscoordinate.backend.service.EventMessageService;
import org.lfenergy.letscoordinate.backend.util.ApplicationContextUtil;
import org.lfenergy.letscoordinate.backend.util.CoordinationFactory;
import org.lfenergy.letscoordinate.backend.util.OpfabUtil;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.lfenergy.letscoordinate.backend.enums.BasicGenericNounEnum.MESSAGE_VALIDATED;
import static org.lfenergy.letscoordinate.backend.enums.BasicGenericNounEnum.PROCESS_SUCCESSFUL;
import static org.lfenergy.letscoordinate.backend.enums.ChangeJsonDataFromWhichEnum.BUSINESS_DATA_IDENTIFIER;
import static org.lfenergy.letscoordinate.backend.enums.ChangeJsonDataFromWhichEnum.HEADER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LetscoKafkaListenerTest {

    private LetscoKafkaListener letscoKafkaListener;
    private OpfabConfig opfabConfig;
    LetscoProperties letscoProperties;
    EventMessageDto eventMessageDto;
    Instant timestamp;
    ObjectMapper objectMapper;

    CoordinationConfig coordinationConfig;
    EventMessageService eventMessageService;
    JsonDataProcessor jsonDataProcessor;
    ExcelDataProcessor excelDataProcessor;

    @Mock
    EventMessageRepository eventMessageRepository;
    @Mock
    OpfabPublisherComponent opfabPublisherComponent;

    @BeforeEach
    public void beforeEach() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        opfabConfig = new OpfabConfig();

        letscoProperties = new LetscoProperties();
        LetscoProperties.InputFile inputFile = new LetscoProperties.InputFile();
        LetscoProperties.InputFile.Validation validation = new LetscoProperties.InputFile.Validation();
        letscoProperties.setCoordination(new LetscoProperties.Coordination());
        inputFile.setValidation(validation);
        letscoProperties.setInputFile(inputFile);

        coordinationConfig = ApplicationContextUtil.initCoordinationConfig();
        eventMessageService = new EventMessageService(coordinationConfig, letscoProperties);
        excelDataProcessor = new ExcelDataProcessor(letscoProperties, coordinationConfig, eventMessageService);
        jsonDataProcessor = new JsonDataProcessor(objectMapper, eventMessageService);

        letscoKafkaListener = new LetscoKafkaListener(jsonDataProcessor, excelDataProcessor, eventMessageRepository,
                opfabPublisherComponent, letscoProperties, opfabConfig, objectMapper);

        timestamp = Instant.parse("2021-03-17T10:15:30.00Z");
        eventMessageDto = EventMessageDto.builder()
                .header(HeaderDto.builder()
                        .noun(PROCESS_SUCCESSFUL.getNoun())
                        .source("source")
                        .messageId("messageId")
                        .timestamp(timestamp)
                        .properties(PropertiesDto.builder()
                                .businessDataIdentifier(BusinessDataIdentifierDto.builder()
                                        .businessApplication("businessApplication")
                                        .messageTypeName("messageTypeName")
                                        .businessDayFrom(Instant.now())
                                        .build()).build()).build()).build();
    }

    @Test
    public void handleLetscoData() throws Exception {

        when(eventMessageRepository.save(any(EventMessage.class))).then(i -> {
            EventMessage savedEventMessage = i.getArgument(0, EventMessage.class);
            savedEventMessage.setId(1L);
            return savedEventMessage;
        });
        letscoKafkaListener.handleLetscoEventMessages(CoordinationFactory.KAFKA_JSON_DATA, 0, "letsco_eventmessage_input", 0L);
    }

    @Test
    public void verifyData() {
        letscoKafkaListener.verifyData(eventMessageDto);
    }

    @Test
    public void verifyData_businessDayToNull() {
        BusinessDataIdentifierDto bdi = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        bdi.setBusinessDayFrom(Instant.parse("2021-08-05T10:00:00.00Z"));
        bdi.setBusinessDayTo(bdi.getBusinessDayFrom().plus(Duration.ofHours(24)).minus(Duration.ofSeconds(1)));
        assertEquals(Instant.parse("2021-08-06T09:59:59.00Z"),bdi.getBusinessDayTo());
    }

    @Test
    public void verifyData_businessDayToNotNull() {
        BusinessDataIdentifierDto bdi = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        bdi.setBusinessDayTo(Instant.parse("2021-08-06T10:00:00.00Z"));
        bdi.setBusinessDayTo(bdi.getBusinessDayTo().minus(Duration.ofSeconds(1)));
        assertEquals(Instant.parse("2021-08-06T09:59:59.00Z"),bdi.getBusinessDayTo());
    }

    @Test
    public void verifyData_CaseIdPresentAndAutoGenerationEnabled() {
        letscoProperties.getCoordination().setEnableCaseIdAutoGeneration(true);
        BusinessDataIdentifierDto bdi = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        bdi.setCaseId("caseId");
        if (letscoProperties.getCoordination().isEnableCaseIdAutoGeneration()) {
            bdi.setCaseId(bdi.getCaseId().orElse(OpfabUtil.generateCaseId(eventMessageDto)));
        }
        assertEquals("caseId",bdi.getCaseId().get());
    }

    @Test
    public void verifyData_CaseIdPresentAndAutoGenerationDisabled() {
        letscoProperties.getCoordination().setEnableCaseIdAutoGeneration(false);
        BusinessDataIdentifierDto bdi = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        bdi.setCaseId("caseId");
        bdi.setBusinessDayFrom(Instant.parse("2021-08-05T10:00:00.00Z"));
        bdi.setBusinessDayTo(Instant.parse("2021-08-06T09:59:59.00Z"));
        if (letscoProperties.getCoordination().isEnableCaseIdAutoGeneration()) {
            bdi.setCaseId(bdi.getCaseId().orElse(OpfabUtil.generateCaseId(eventMessageDto)));
        }
        assertEquals("caseId",bdi.getCaseId().get());
    }

    @Test
    public void verifyData_CaseIdNotPresentAndAutoGenerationEnabled() {
        letscoProperties.getCoordination().setEnableCaseIdAutoGeneration(true);
        BusinessDataIdentifierDto bdi = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        bdi.setBusinessDayFrom(Instant.parse("2021-08-05T10:00:00.00Z"));
        bdi.setBusinessDayTo(Instant.parse("2021-08-06T09:59:59.00Z"));
        if (letscoProperties.getCoordination().isEnableCaseIdAutoGeneration()) {
            bdi.setCaseId(bdi.getCaseId().orElse(OpfabUtil.generateCaseId(eventMessageDto)));
        }
        assertEquals("source_businessApplication_messageTypeName_2021-08-05T10:00:00Z_2021-08-06T09:59:59Z",bdi.getCaseId().get());
    }

    @Test
    public void verifyData_CaseIdNotPresentAndAutoGenerationDisabled() {
        letscoProperties.getCoordination().setEnableCaseIdAutoGeneration(false);
        BusinessDataIdentifierDto bdi = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        bdi.setBusinessDayFrom(Instant.parse("2021-08-05T10:00:00.00Z"));
        bdi.setBusinessDayTo(Instant.parse("2021-08-06T09:59:59.00Z"));
        if (letscoProperties.getCoordination().isEnableCaseIdAutoGeneration()) {
            bdi.setCaseId(bdi.getCaseId().orElse(OpfabUtil.generateCaseId(eventMessageDto)));
        }
        assertTrue(bdi.getCaseId().isEmpty());
    }

    @Test
    public void isGenericNoun() {
        assertTrue(letscoKafkaListener.isGenericNoun(eventMessageDto.getHeader().getNoun()));
    }

    @Test
    public void isGenericNoun_CompatibleNoun() {
        Map<BasicGenericNounEnum, List<String>> genericNouns = Map.of(BasicGenericNounEnum.PROCESS_ACTION,
                List.of("myNoun"));
        letscoProperties.getInputFile().setGenericNouns(genericNouns);
        eventMessageDto.getHeader().setNoun("myNoun");
        assertTrue(letscoKafkaListener.isGenericNoun(eventMessageDto.getHeader().getNoun()));
    }

    @Test
    public void isGenericNoun_NotCompatibleNoun() {
        eventMessageDto.getHeader().setNoun("myNoun");
        assertFalse(letscoKafkaListener.isGenericNoun(eventMessageDto.getHeader().getNoun()));
    }

    @Test
    public void changeNounIfNeeded() {
        Map<BasicGenericNounEnum, List<String>> genericNouns = Map.of(BasicGenericNounEnum.PROCESS_ACTION,
                List.of("myNoun"));
        letscoProperties.getInputFile().setGenericNouns(genericNouns);
        eventMessageDto.getHeader().setNoun("myNoun");
        letscoKafkaListener.changeNounIfNeeded(eventMessageDto.getHeader());
        assertEquals("ProcessAction", eventMessageDto.getHeader().getNoun());
    }

    @Test
    public void changeSourceIfNeeded_NoChanges() {
        letscoKafkaListener.changeSourceIfNeeded(eventMessageDto);
        assertEquals("source", eventMessageDto.getHeader().getSource());
    }

    @Test
    public void changeSourceIfNeeded_FromBusinessDataIdentifier() {
        Map<String, LetscoProperties.InputFile.ChangeSource> changeSourceMap = Map.of("source",
                new LetscoProperties.InputFile.ChangeSource(BUSINESS_DATA_IDENTIFIER, "businessApplication"));
        letscoProperties.getInputFile().getValidation().setChangeSource(changeSourceMap);
        letscoKafkaListener.changeSourceIfNeeded(eventMessageDto);
        assertEquals("businessApplication", eventMessageDto.getHeader().getSource());
    }

    @Test
    public void changeSourceIfNeeded_FromHeader() {
        Map<String, LetscoProperties.InputFile.ChangeSource> changeSourceMap = Map.of("source",
                new LetscoProperties.InputFile.ChangeSource(HEADER, "messageId"));
        letscoProperties.getInputFile().getValidation().setChangeSource(changeSourceMap);
        letscoKafkaListener.changeSourceIfNeeded(eventMessageDto);
        assertEquals("messageId", eventMessageDto.getHeader().getSource());
    }

    @Test
    public void changeMessageTypeNameIfNeeded() {
        BusinessDataIdentifierDto bdi = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        Map<String, String> changeMessageTypeNameMap = Map.of("messageTypeName", "newMessageTypeName");
        letscoProperties.getInputFile().getValidation().setChangeMessageTypeName(changeMessageTypeNameMap);
        letscoKafkaListener.changeMessageTypeNameIfNeeded(bdi);
        assertEquals("newMessageTypeName", bdi.getMessageTypeName());
    }

    @Test
    public void changeMessageTypeNameIfNeeded_NoChange() {
        BusinessDataIdentifierDto bdi = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        letscoKafkaListener.changeMessageTypeNameIfNeeded(bdi);
        assertEquals("messageTypeName", bdi.getMessageTypeName());
    }

    @Test
    public void ignoreProcessIfNeeded() {
        String process = OpfabUtil.generateProcessKey(eventMessageDto, true);
        List<String> ignoreProcesses = List.of(process);
        letscoProperties.getInputFile().getValidation().setIgnoreProcesses(ignoreProcesses);
        assertThrows(IgnoreProcessException.class, () ->
                letscoKafkaListener.ignoreProcessIfNeeded(process));
    }

    @Test
    public void ignoreProcessIfNeeded_ProcessNotIgnored() {
        String process = OpfabUtil.generateProcessKey(eventMessageDto, true);
        letscoKafkaListener.ignoreProcessIfNeeded(process);
    }

    @Test
    public void ignoreMessageTypeNameIfNeeded() {
        String messageTypeName =
                eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getMessageTypeName();
        List<String> ignoreMessageTypeNames = List.of(messageTypeName);
        letscoProperties.getInputFile().getValidation().setIgnoreMessageTypeNames(ignoreMessageTypeNames);
        assertThrows(IgnoreProcessException.class, () ->
                letscoKafkaListener.ignoreMessageTypeNameIfNeeded(messageTypeName));
    }

    @Test
    public void ignoreMessageTypeNameIfNeeded_MessageTypeNameNotIgnored() {
        letscoKafkaListener.ignoreProcessIfNeeded(
                eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getMessageTypeName());
    }

    @Test
    public void ignorePositiveTechnicalQualityCheck() {
        eventMessageDto.getHeader().setNoun(MESSAGE_VALIDATED.getNoun());
        eventMessageDto.setPayload(PayloadDto.builder()
                .validation(ValidationDto.builder()
                        .result(ValidationSeverityEnum.OK)
                        .validationType(ValidationTypeEnum.TECHNICAL).build()).build());
        assertThrows(PositiveTechnicalQualityCheckException.class, () ->
                letscoKafkaListener.ignorePositiveTechnicalQualityCheck(eventMessageDto));
    }

    @Test
    public void ignorePositiveTechnicalQualityCheck_NotQualityCheck() {
        letscoKafkaListener.ignorePositiveTechnicalQualityCheck(eventMessageDto);
    }

    @Test
    public void ignorePositiveTechnicalQualityCheck_QualityCheckNotOk() {
        eventMessageDto.getHeader().setNoun(MESSAGE_VALIDATED.getNoun());
        eventMessageDto.setPayload(PayloadDto.builder()
                .validation(ValidationDto.builder()
                        .result(ValidationSeverityEnum.ERROR)
                        .validationType(ValidationTypeEnum.TECHNICAL).build()).build());
        letscoKafkaListener.ignorePositiveTechnicalQualityCheck(eventMessageDto);
    }

    @Test
    public void ignorePositiveTechnicalQualityCheck_QualityCheckOkNotTechnical() {
        eventMessageDto.getHeader().setNoun(MESSAGE_VALIDATED.getNoun());
        eventMessageDto.setPayload(PayloadDto.builder()
                .validation(ValidationDto.builder()
                        .result(ValidationSeverityEnum.OK)
                        .validationType(ValidationTypeEnum.BUSINESS).build()).build());
        letscoKafkaListener.ignorePositiveTechnicalQualityCheck(eventMessageDto);
    }
}
