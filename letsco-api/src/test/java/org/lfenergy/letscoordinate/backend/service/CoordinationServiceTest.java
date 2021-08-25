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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vavr.control.Validation;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.backend.component.OpfabPublisherComponent;
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.config.LetscoProperties;
import org.lfenergy.letscoordinate.backend.config.OpfabConfig;
import org.lfenergy.letscoordinate.backend.dto.coordination.CoordinationResponseDataDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageWrapperDto;
import org.lfenergy.letscoordinate.backend.enums.*;
import org.lfenergy.letscoordinate.backend.kafka.LetscoKafkaProducer;
import org.lfenergy.letscoordinate.backend.model.Coordination;
import org.lfenergy.letscoordinate.backend.model.CoordinationLttdQueue;
import org.lfenergy.letscoordinate.backend.model.EventMessage;
import org.lfenergy.letscoordinate.backend.model.EventMessageFile;
import org.lfenergy.letscoordinate.backend.processor.ExcelDataProcessor;
import org.lfenergy.letscoordinate.backend.repository.*;
import org.lfenergy.letscoordinate.backend.util.ApplicationContextUtil;
import org.lfenergy.letscoordinate.backend.util.Constants;
import org.lfenergy.letscoordinate.backend.util.CoordinationFactory;
import org.opfab.cards.model.Card;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    CoordinationLttdQueueRepository coordinationLttdQueueRepository;
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
    @MockBean
    OpfabPublisherComponent opfabPublisherComponent;
    @MockBean
    RestTemplate restTemplate;

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
                coordinationLttdQueueRepository,
                objectMapper,
                eventMessageRepository,
                opfabConfig,
                ApplicationContextUtil.initCoordinationConfig(),
                letscoKafkaProducer,
                excelDataProcessor,
                letscoProperties,
                restTemplate
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
                                        .response(CoordinationEntityRaResponseEnum.NOK)
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
                () -> assertEquals(CoordinationEntityRaResponseEnum.NOK, validation.get().getCoordinationRas().get(0).getCoordinationRaAnswers().get(0).getAnswer()),
                () -> assertEquals("Explanation 1", validation.get().getCoordinationRas().get(0).getCoordinationRaAnswers().get(0).getExplanation()),
                () -> assertEquals("Not ok!", validation.get().getCoordinationRas().get(0).getCoordinationRaAnswers().get(0).getComment())
        );
    }

    @Test
    public void initAndSavaCoordination_findByIdReturnsEmpty() {
        when(eventMessageRepository.findById(anyLong())).thenReturn(Optional.empty());

        Card card = new Card();
        card.setProcess("process");

        Coordination coordination = coordinationService.initAndSaveCoordination(card, 1L);

        assertNull(coordination);
    }

    @Test
    public void initAndSavaCoordination_businessDayFromIsBeforeTimestamp_lttdNull() {
        when(coordinationRepository.save(any(Coordination.class))).then(i -> {
            Coordination c = i.getArgument(0, Coordination.class);
            c.setId(1L);
            return c;
        });
        when(eventMessageRepository.findById(anyLong())).thenReturn(Optional.of(CoordinationFactory.initEventMessage(99L, FileTypeEnum.EXCEL)));

        Card card = new Card();
        card.setProcess("process");

        Coordination coordination = coordinationService.initAndSaveCoordination(card, 1L);

        assertAll(
                () -> assertNotNull(coordination),
                () -> assertEquals(1L, coordination.getId()),
                () -> assertEquals(99L, coordination.getEventMessage().getId()),
                () -> assertNull(coordination.getLttd()),
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
    public void initAndSavaCoordination_businessDayFromIsAfterTimestamp_lttdNotNull() {
        EventMessage eventMessage = CoordinationFactory.initEventMessage(99L, FileTypeEnum.EXCEL);
        eventMessage.setBusinessDayFrom(Instant.parse("2021-05-31T12:00:00Z"));
        letscoProperties.getCoordination().getLttd().getSpecificLttd().put("process", LttdEnum.AT_8_PM);

        when(coordinationRepository.save(any(Coordination.class))).then(i -> {
            Coordination c = i.getArgument(0, Coordination.class);
            c.setId(1L);
            return c;
        });
        when(eventMessageRepository.findById(anyLong())).thenReturn(Optional.of(eventMessage));

        Card card = new Card();
        card.setProcess("process");
        Coordination coordination = coordinationService.initAndSaveCoordination(card, 1L);

        assertAll(
                () -> assertNotNull(coordination),
                () -> assertEquals(1L, coordination.getId()),
                () -> assertEquals(99L, coordination.getEventMessage().getId()),
                () -> assertNotNull(coordination.getLttd()),
                () -> assertEquals(Instant.parse("2021-05-31T05:13:00Z"), coordination.getPublishDate()),
                () -> assertEquals(Instant.parse("2021-05-31T05:13:00Z"), coordination.getStartDate()),
                () -> assertEquals(Instant.parse("2021-05-31T23:59:59Z"), coordination.getEndDate()),
                () -> assertEquals(2, coordination.getCoordinationRas().size()),
                () -> assertEquals("Event A", coordination.getCoordinationRas().get(0).getEvent()),
                () -> assertEquals("Constraint A", coordination.getCoordinationRas().get(0).getConstraintt()),
                () -> assertEquals("RemedialActions A", coordination.getCoordinationRas().get(0).getRemedialAction())
        );
    }

    @Test
    public void generateExcelOutputFile() throws IOException {
        coordinationService.sendOutputFileToKafka(CoordinationFactory.initEventMessage(FileTypeEnum.EXCEL));
    }

    @Test
    public void generateJsonOutputFile() throws IOException {
        when(objectMapper.readValue((byte[])any(), any(Class.class))).then(i ->
                new ObjectMapper().registerModule(new JavaTimeModule()).readValue((byte[]) i.getArgument(0), EventMessageWrapperDto.class)
        );
        coordinationService.sendOutputFileToKafka(CoordinationFactory.initEventMessage(FileTypeEnum.JSON));
    }

    @Test
    public void sendCoordinationFileCard_INPUT() {
        coordinationService.sendCoordinationFileCard(new Card(), FileDirectionEnum.INPUT);
    }

    @Test
    public void sendCoordinationFileCard_OUTPUT() {
        coordinationService.sendCoordinationFileCard(new Card(), FileDirectionEnum.OUTPUT);
    }

    @Test
    public void applyCoordinationAnswersToEventMessage_shouldFillEventMessage() throws IOException {
        when(eventMessageRepository.save(any(EventMessage.class))).then(i -> {
            EventMessage eventMessage = i.getArgument(0, EventMessage.class);
            eventMessage.setId(1L);
            return eventMessage;
        });
        when(objectMapper.readValue((byte[]) any(), any(Class.class))).thenReturn(EventMessageWrapperDto.builder()
                .eventMessage(CoordinationFactory.initEventMessageDto())
                .build());

        Coordination coordination = CoordinationFactory.initCoordination(FileTypeEnum.JSON);

        assertAll(
                () -> assertNotNull(coordination.getEventMessage()),
                () -> assertNull(coordination.getEventMessage().getCoordinationStatus()),
                () -> assertTrue(CollectionUtils.isEmpty(coordination.getEventMessage().getEventMessageCoordinationComments())),
                () -> assertTrue(coordination.getEventMessage().getTimeseries().get(0).getTimeserieDatas().get(0).getTimeserieDataDetailses()
                        .stream().filter(d -> Constants.REMEDIAL_ACTIONS_KEY.equals(d.getLabel())).findFirst()
                        .get().getTimeserieDataDetailsResults().isEmpty()),
                () -> assertTrue(coordination.getEventMessage().getTimeseries().get(0).getTimeserieDatas().get(1).getTimeserieDataDetailses()
                        .stream().filter(d -> Constants.REMEDIAL_ACTIONS_KEY.equals(d.getLabel())).findFirst()
                        .get().getTimeserieDataDetailsResults().isEmpty())
        );

        EventMessage eventMessage = coordinationService.applyCoordinationAnswersToEventMessage(coordination);

        assertAll(
                () -> assertNotNull(eventMessage),
                () -> assertEquals(CoordinationStatusEnum.REJ, eventMessage.getCoordinationStatus()),
                () -> assertEquals(1, eventMessage.getEventMessageCoordinationComments().size()),
                () -> assertFalse(eventMessage.getTimeseries().get(0).getTimeserieDatas().get(0).getTimeserieDataDetailses()
                        .stream().filter(d -> Constants.REMEDIAL_ACTIONS_KEY.equals(d.getLabel())).findFirst()
                        .get().getTimeserieDataDetailsResults().isEmpty()),
                () -> assertFalse(eventMessage.getTimeseries().get(0).getTimeserieDatas().get(1).getTimeserieDataDetailses()
                        .stream().filter(d -> Constants.REMEDIAL_ACTIONS_KEY.equals(d.getLabel())).findFirst()
                        .get().getTimeserieDataDetailsResults().isEmpty())
        );
    }

    @Test
    void getEventMessageFileIfExists_fileNotExists() {
        when(eventMessageRepository.findById(anyLong())).thenReturn(Optional.empty());
        Optional<EventMessageFile> file = coordinationService.getEventMessageFileIfExists(1L, FileDirectionEnum.INPUT);
        assertTrue(file.isEmpty());
    }

    @Test
    void getEventMessageFileIfExists_fileExists() {
        EventMessageFile eventMessageFile = EventMessageFile.builder()
                .id(5L)
                .fileDirection(FileDirectionEnum.INPUT)
                .creationDate(Instant.parse("2008-08-08T08:08:08Z"))
                .build();
        when(eventMessageRepository.findById(anyLong())).thenReturn(
                Optional.of(EventMessage.builder().eventMessageFiles(Arrays.asList(eventMessageFile)).build())
        );
        Optional<EventMessageFile> file = coordinationService.getEventMessageFileIfExists(1L, FileDirectionEnum.INPUT);
        assertAll(
                () -> assertTrue(file.isPresent()),
                () -> assertEquals(5L, file.get().getId()),
                () -> assertEquals(FileDirectionEnum.INPUT, file.get().getFileDirection()),
                () -> assertEquals(Instant.parse("2008-08-08T08:08:08Z"), file.get().getCreationDate())
        );
    }

    @Test
    void getLttdExpiredCoordinations_shouldReturnList() {
        when(coordinationLttdQueueRepository.findByLttdLessThan(any(Instant.class))).thenReturn(Arrays.asList(
                CoordinationLttdQueue.builder().id(7L).build()
        ));
        List resultList = coordinationService.getLttdExpiredCoordinations();
        assertAll(
                () -> assertNotNull(resultList),
                () -> assertFalse(resultList.isEmpty())
        );
    }

    @Test
    void updateCoordinationProcessStatusAndRemoveItFromLttdQueue_statusFinished() {
        CoordinationLttdQueue coordinationLttdQueue = CoordinationLttdQueue.builder()
                .coordination(Coordination.builder().build())
                .build();
        coordinationService.updateCoordinationProcessStatusAndRemoveItFromLttdQueue(coordinationLttdQueue);
        assertEquals(CoordinationProcessStatusEnum.FINISHED,coordinationLttdQueue.getCoordination().getStatus());
    }

}