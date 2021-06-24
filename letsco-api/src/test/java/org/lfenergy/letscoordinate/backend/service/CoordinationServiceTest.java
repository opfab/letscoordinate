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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vavr.control.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.config.LetscoProperties;
import org.lfenergy.letscoordinate.backend.config.OpfabConfig;
import org.lfenergy.letscoordinate.backend.dto.coordination.CoordinationResponseDataDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageWrapperDto;
import org.lfenergy.letscoordinate.backend.enums.CoordinationAnswerEnum;
import org.lfenergy.letscoordinate.backend.enums.FileTypeEnum;
import org.lfenergy.letscoordinate.backend.kafka.LetscoKafkaProducer;
import org.lfenergy.letscoordinate.backend.model.Coordination;
import org.lfenergy.letscoordinate.backend.model.EventMessage;
import org.lfenergy.letscoordinate.backend.processor.ExcelDataProcessor;
import org.lfenergy.letscoordinate.backend.repository.CoordinationGeneralCommentRepository;
import org.lfenergy.letscoordinate.backend.repository.CoordinationRaAnswerRepository;
import org.lfenergy.letscoordinate.backend.repository.CoordinationRepository;
import org.lfenergy.letscoordinate.backend.repository.EventMessageRepository;
import org.lfenergy.letscoordinate.backend.util.ApplicationContextUtil;
import org.lfenergy.letscoordinate.backend.util.CoordinationFactory;
import org.mockito.Spy;
import org.opfab.cards.model.Card;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class CoordinationServiceTest {
    CoordinationService coordinationService;
    OpfabConfig opfabConfig;
    LetscoProperties letscoProperties;
    ExcelDataProcessor excelDataProcessor;
    CoordinationConfig coordinationConfig;
    EventMessageService eventMessageService;
    @MockBean
    CoordinationRepository coordinationRepository;
    @MockBean
    EventMessageRepository eventMessageRepository;
    @MockBean
    ObjectMapper objectMapper;
    @MockBean
    CoordinationGeneralCommentRepository coordinationGeneralCommentRepository;
    @MockBean
    CoordinationRaAnswerRepository coordinationRaAnswerRepository;
    @MockBean
    LetscoKafkaProducer letscoKafkaProducer;

    @BeforeEach
    public void before() {
        OpfabConfig.OpfabEntityRecipients entityRecipient = new OpfabConfig.OpfabEntityRecipients();
        entityRecipient.setAddRscs(true);
        entityRecipient.setNotAllowed("sendingUser");
        Map<String, OpfabConfig.OpfabEntityRecipients> entityRecipientsMap = new HashMap<>();
        entityRecipientsMap.put("processKey", entityRecipient);
        opfabConfig = OpfabConfig.builder()
                .entityRecipients(entityRecipientsMap)
                .build();
        eventMessageService = new EventMessageService(coordinationConfig, letscoProperties);
        letscoProperties = ApplicationContextUtil.initLetscoProperties();
        coordinationConfig = ApplicationContextUtil.initCoordinationConfig();
        excelDataProcessor = new ExcelDataProcessor(letscoProperties, coordinationConfig, eventMessageService);
        coordinationService = new CoordinationService (
                coordinationRepository,
                coordinationRaAnswerRepository,
                coordinationGeneralCommentRepository,
                objectMapper,
                eventMessageRepository,
                opfabConfig,
                ApplicationContextUtil.initCoordinationConfig(),
                letscoKafkaProducer,
                excelDataProcessor,
                letscoProperties
        );
    }

    @Test
    public void saveAnswersAndCheckIfAllTsosHaveAnswered_shouldReturnValidValidation() {
        when(objectMapper.convertValue(any(), any(Class.class))).thenReturn(
                CoordinationResponseDataDto.builder()
                        .validationDataId(1L)
                        .generalComment("generalComment")
                        .formData(Arrays.asList(
                                CoordinationResponseDataDto.FormData.builder()
                                        .id(2L)
                                        .response(CoordinationAnswerEnum.NOK)
                                        .explanation("Explanation 1")
                                        .comment("Not ok!")
                                        .build()
                        ))
                        .build()
        );
        when(coordinationRepository.findByEventMessage_Id(anyLong())).thenReturn(Optional.of(CoordinationFactory.initCoordination(FileTypeEnum.EXCEL)));
        Validation<Boolean, Coordination> validation = coordinationService.saveAnswersAndCheckIfAllTsosHaveAnswered(new Card());
        assertAll(
                () -> assertNotNull(validation),
                () -> assertTrue(validation.isValid()),
                () -> assertEquals(1L, validation.get().getId()),
                () -> assertNotNull(validation.get().getEventMessage()),
                () -> assertEquals("coordinationProcessKey", validation.get().getProcessKey()),
                () -> assertEquals(Instant.parse("2021-05-31T05:13:00Z"), validation.get().getPublishDate()),
                () -> assertEquals(Instant.parse("2021-05-31T00:00:00Z"), validation.get().getStartDate()),
                () -> assertEquals(Instant.parse("2021-05-31T23:59:59Z"), validation.get().getEndDate()),
                () -> assertEquals(2, validation.get().getCoordinationRas().size()),
                () -> assertEquals("Event A", validation.get().getCoordinationRas().get(0).getEvent()),
                () -> assertEquals("Constraint A", validation.get().getCoordinationRas().get(0).getConstraintt()),
                () -> assertEquals("RemedialActions A", validation.get().getCoordinationRas().get(0).getRemedialAction()),
                () -> assertEquals(2, validation.get().getCoordinationRas().get(0).getCoordinationRaAnswers().size()),
                () -> assertEquals(3L, validation.get().getCoordinationRas().get(0).getCoordinationRaAnswers().get(0).getId()),
                () -> assertEquals("10XFR-RTE------Q", validation.get().getCoordinationRas().get(0).getCoordinationRaAnswers().get(0).getEicCode()),
                () -> assertEquals(CoordinationAnswerEnum.NOK, validation.get().getCoordinationRas().get(0).getCoordinationRaAnswers().get(0).getAnswer()),
                () -> assertEquals("Explanation 1", validation.get().getCoordinationRas().get(0).getCoordinationRaAnswers().get(0).getExplanation()),
                () -> assertEquals("Not ok!", validation.get().getCoordinationRas().get(0).getCoordinationRaAnswers().get(0).getComment())
        );
    }

    @Test
    public void initAndSavaCoordination_shouldReturnCoordination() {
        when(coordinationRepository.save(any(Coordination.class))).then(i -> {
            Coordination c = i.getArgument(0, Coordination.class);
            c.setId(1L);
            return c;
        });
        when(eventMessageRepository.findById(anyLong())).thenReturn(Optional.of(CoordinationFactory.initEventMessage(99L, FileTypeEnum.EXCEL)));

        Card card = new Card();
        card.setProcess("process");
        EventMessageDto eventMessageDto = CoordinationFactory.initEventMessageDto();

        Coordination coordination = coordinationService.initAndSaveCoordination(card, eventMessageDto, 1L);

        assertAll(
                () -> assertNotNull(coordination),
                () -> assertEquals(1L, coordination.getId()),
                () -> assertEquals(99L, coordination.getEventMessage().getId()),
                () -> assertEquals(Instant.parse("2021-05-31T05:13:00Z"), coordination.getPublishDate()),
                () -> assertEquals(Instant.parse("2021-05-31T00:00:00Z"), coordination.getStartDate()),
                () -> assertEquals(Instant.parse("2021-05-31T23:59:59Z"), coordination.getEndDate()),
                () -> assertEquals(2, coordination.getCoordinationRas().size()),
                () -> assertEquals("Event A", coordination.getCoordinationRas().get(0).getEvent()),
                () -> assertEquals("Constraint A", coordination.getCoordinationRas().get(0).getConstraintt()),
                () -> assertEquals("RemedialActions A", coordination.getCoordinationRas().get(0).getRemedialAction())
        );
    }

    @Test
    public void generateExcelOutputFile() throws IOException {
        coordinationService.generateOutputFile(CoordinationFactory.initCoordination(FileTypeEnum.EXCEL));
    }

    @Test
    public void generateJsonOutputFile() throws IOException {
        when(objectMapper.readValue((byte[])any(), any(Class.class))).then(i ->
                new ObjectMapper().registerModule(new JavaTimeModule()).readValue((byte[]) i.getArgument(0), EventMessageWrapperDto.class)
        );
        coordinationService.generateOutputFile(CoordinationFactory.initCoordination(FileTypeEnum.JSON));
    }

}