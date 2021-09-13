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

package org.lfenergy.letscoordinate.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.backend.component.OpfabPublisherComponent;
import org.lfenergy.letscoordinate.backend.enums.FileTypeEnum;
import org.lfenergy.letscoordinate.backend.kafka.LetscoKafkaProducer;
import org.lfenergy.letscoordinate.backend.model.Coordination;
import org.lfenergy.letscoordinate.backend.model.CoordinationLttdQueue;
import org.lfenergy.letscoordinate.backend.model.EventMessage;
import org.lfenergy.letscoordinate.backend.repository.EventMessageRepository;
import org.lfenergy.letscoordinate.backend.service.CoordinationService;
import org.lfenergy.letscoordinate.backend.util.CoordinationFactory;
import org.opfab.cards.model.Card;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CoordinationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    EventMessageRepository eventMessageRepository;
    @MockBean
    CoordinationService coordinationService;
    @MockBean
    OpfabPublisherComponent opfabPublisherComponent;
    @MockBean
    LetscoKafkaProducer letscoKafkaProducer;

    private CoordinationController coordinationController;

    @BeforeEach
    public void init() {
        coordinationController = new CoordinationController(coordinationService, opfabPublisherComponent);
    }

    @Test
    @WithMockCustomUser
    public void coordinationCallback_entitiesTotallyRespond_shouldReturn200() throws Exception {
        when(coordinationService.saveAnswersAndCheckIfAllTsosHaveAnswered(any(Card.class))).thenReturn(Validation.valid(Coordination.builder().build()));
        when(coordinationService.sendOutputFileToKafka(any(EventMessage.class))).thenReturn(true);
        when(opfabPublisherComponent.publishOpfabCoordinationResultCard(any(Coordination.class))).thenReturn(new Card());
        mockMvc.perform(post("/letsco/api/v1/coordination")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(new Card())))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void coordinationCallback_entitiesPartiallyRespond_shouldReturn200() throws Exception {
        when(coordinationService.saveAnswersAndCheckIfAllTsosHaveAnswered(any(Card.class))).thenReturn(Validation.invalid(Boolean.FALSE));
        mockMvc.perform(post("/letsco/api/v1/coordination")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(new Card())))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void manageCardsLTTD_getLttdExpiredCoordinations_throwsException() {
        when(coordinationService.getLttdExpiredCoordinations()).thenThrow(RuntimeException.class);
        coordinationController.manageCardsLTTD();
    }

    @Test
    public void manageCardsLTTD_updateCoordinationProcessStatusAndRemoveItFromLttdQueue_throwsException() {
        doThrow(RuntimeException.class).when(coordinationService).updateCoordinationProcessStatusAndRemoveItFromLttdQueue(any());
        coordinationController.manageCardsLTTD();
    }

    @Test
    public void manageCardsLTTD_sendOutputFileToKafka_true() {
        when(coordinationService.getLttdExpiredCoordinations()).thenReturn(
                Arrays.asList(CoordinationLttdQueue.builder()
                        .id(1L)
                        .lttd(Instant.parse("1999-12-31T23:59:59Z"))
                        .coordination(CoordinationFactory.initCoordination(FileTypeEnum.JSON))
                        .build())
        );
        when(coordinationService.sendOutputFileToKafka(any())).thenReturn(Boolean.TRUE);
        coordinationController.manageCardsLTTD();
    }

    @Test
    public void manageCardsLTTD_sendOutputFileToKafka_false() {
        when(coordinationService.getLttdExpiredCoordinations()).thenReturn(
                Arrays.asList(CoordinationLttdQueue.builder()
                        .id(1L)
                        .lttd(Instant.parse("1999-12-31T23:59:59Z"))
                        .coordination(CoordinationFactory.initCoordination(FileTypeEnum.JSON))
                        .build())
        );
        when(coordinationService.sendOutputFileToKafka(any())).thenReturn(Boolean.FALSE);
        coordinationController.manageCardsLTTD();
    }

}
