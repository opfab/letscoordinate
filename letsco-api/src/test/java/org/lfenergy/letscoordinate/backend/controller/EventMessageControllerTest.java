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
import org.lfenergy.letscoordinate.backend.model.Coordination;
import org.lfenergy.letscoordinate.backend.model.EventMessage;
import org.lfenergy.letscoordinate.backend.model.EventMessageFile;
import org.lfenergy.letscoordinate.backend.repository.EventMessageRepository;
import org.lfenergy.letscoordinate.backend.service.CoordinationService;
import org.opfab.cards.model.Card;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class EventMessageControllerTest {

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

    MockMultipartFile validMultipartFile;
    MockMultipartFile validMultipartFileWithLowercaseTitles;
    MockMultipartFile invalidMultipartFile;

    @BeforeEach
    public void before() throws IOException {
        File file1 = new File("src/test/resources/validTestFile_1.xlsx");
        validMultipartFile = new MockMultipartFile("file", file1.getName(), null, new FileInputStream(file1));

        File file2 = new File("src/test/resources/validTestFileWithLowercaseTitles.xlsx");
        validMultipartFileWithLowercaseTitles = new MockMultipartFile("file", file2.getName(), null, new FileInputStream(file2));

        File file3 = new File("src/test/resources/invalidTestFile_1.xlsx");
        invalidMultipartFile = new MockMultipartFile("file", file3.getName(), null, new FileInputStream(file3));
    }

    @Test
    @WithMockCustomUser
    public void uploadedExcelToJson_shouldReturn200() throws Exception {
        mockMvc.perform(multipart("/letsco/api/v1/upload/validate").file(validMultipartFile)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventMessage.header.noun").value("ServiceA_CalculationResults_1"))
                .andExpect(jsonPath("$.eventMessage.header.source").value("SERVICE_A"))
                .andExpect(jsonPath("$.eventMessage.header.messageId").value("b071aa50097f49f1bd69e82a070084b6"))
                .andExpect(jsonPath("$.eventMessage.payload.text", hasSize(2)))
                .andExpect(jsonPath("$.eventMessage.payload.links", hasSize(3)))
                .andExpect(jsonPath("$.eventMessage.payload.rscKpi", hasSize(3)))
                .andExpect(jsonPath("$.eventMessage.payload.timeserie", hasSize(4)));
    }

    @Test
    @WithMockCustomUser
    public void uploadedExcelToJson_shouldReturn400() throws Exception {
        mockMvc.perform(multipart("/letsco/api/v1/upload/validate").file(invalidMultipartFile)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_FILE"))
                .andExpect(jsonPath("$.messages", hasSize(2)));
    }

    @Test
    @WithMockCustomUser
    public void uploadedExcelsToJson_shouldReturn200() throws Exception {
        when(eventMessageRepository.save(any(EventMessage.class))).then(i -> i.getArgument(0, EventMessage.class));
        mockMvc.perform(multipart("/letsco/api/v1/upload/save").file(validMultipartFile)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void uploadedExcelsToJson_shouldReturn400() throws Exception {
        when(eventMessageRepository.save(any(EventMessage.class))).thenThrow(RuntimeException.class);
        mockMvc.perform(multipart("/letsco/api/v1/upload/save").file(invalidMultipartFile)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_FILE"))
                .andExpect(jsonPath("$.messages", hasSize(2)));
    }

    @Test
    @WithMockCustomUser
    public void deleteEventMessageById_shouldReturn200() throws Exception {
        doNothing().when(eventMessageRepository).deleteById(anyLong());
        mockMvc.perform(delete("/letsco/api/v1/eventmessages/1")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void deleteEventMessageById_shouldReturn500() throws Exception {
        doThrow(RuntimeException.class).when(eventMessageRepository).deleteById(anyLong());
        mockMvc.perform(delete("/letsco/api/v1/eventmessages/1")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(jsonPath("$.code").value("ERROR"))
                .andExpect(jsonPath("$.messages", hasSize(1)));
    }

    @Test
    @WithMockCustomUser
    public void deleteAllEventMessages_shouldReturn200() throws Exception {
        doNothing().when(eventMessageRepository).deleteAll();
        mockMvc.perform(delete("/letsco/api/v1/eventmessages")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void deleteAllEventMessages_shouldReturn500() throws Exception {
        doThrow(RuntimeException.class).when(eventMessageRepository).deleteAll();
        mockMvc.perform(delete("/letsco/api/v1/eventmessages")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(jsonPath("$.code").value("ERROR"))
                .andExpect(jsonPath("$.messages", hasSize(1)));
    }

    @Test
    @WithMockCustomUser
    public void coordinationCallback_entitiesTotallyRespond_shouldReturn200() throws Exception {
        when(coordinationService.saveAnswersAndCheckIfAllTsosHaveAnswered(any(Card.class))).thenReturn(Validation.valid(Coordination.builder().build()));
        doNothing().when(opfabPublisherComponent).publishOpfabCoordinationResultCard(any(Coordination.class));
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
    @WithMockCustomUser
    public void generateOutputFile_shouldReturn200() throws Exception {
        mockMvc.perform(get("/letsco/api/v1/eventmessages/{id}/generate-output-file", 1)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void getOutputFile_fileExists_shouldReturn200() throws Exception {
        when(coordinationService.getEventMessageOutputFileIfExists(anyLong())).thenReturn(Optional.of(EventMessageFile.builder().build()));
        mockMvc.perform(get("/letsco/api/v1/eventmessages/{id}/output-file", 1)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void getOutputFile_fileNotExists_shouldReturn404() throws Exception {
        when(coordinationService.getEventMessageOutputFileIfExists(anyLong())).thenReturn(Optional.empty());
        mockMvc.perform(get("/letsco/api/v1/eventmessages/{id}/output-file", 1)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    public void downloadOutputFile_fileExists_shouldReturn200() throws Exception {
        when(coordinationService.getEventMessageOutputFileIfExists(anyLong())).thenReturn(Optional.of(EventMessageFile.builder()
                .fileName("test")
                .fileContent(new byte[] {})
                .build()));
        mockMvc.perform(get("/letsco/api/v1/eventmessages/{id}/download-output-file", 1)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void downloadOutputFile_fileNotExists_shouldReturn404() throws Exception {
        when(coordinationService.getEventMessageOutputFileIfExists(anyLong())).thenReturn(Optional.empty());
        mockMvc.perform(get("/letsco/api/v1/eventmessages/{id}/download-output-file", 1)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

}
