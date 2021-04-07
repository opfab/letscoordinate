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

package org.lfenergy.letscoordinate.scanner.service;

import io.vavr.control.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.scanner.config.LetscoProperties;
import org.lfenergy.letscoordinate.scanner.dto.ProcessedFileDto;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class EventMessageServiceTest {

    EventMessageService eventMessageService;
    LetscoProperties letscoProperties;
    MockMultipartFile validMultipartFile;
    MockMultipartFile validMultipartFileWithLowercaseTitles;
    MockMultipartFile invalidMultipartFile;
    @Mock
    RestTemplate restTemplate;

    @BeforeEach
    public void before() throws IOException {
        letscoProperties = LetscoProperties.builder()
                .backend(LetscoProperties.Backend.builder()
                        .baseUrl("http://test_base_url")
                        .build())
                .build();
        eventMessageService = new EventMessageService(letscoProperties, restTemplate);

        File file1 = new File("src/test/resources/validTestFile_1.xlsx");
        validMultipartFile = new MockMultipartFile("file", file1.getName(), null, new FileInputStream(file1));

        File file2 = new File("src/test/resources/validTestFileWithLowercaseTitles.xlsx");
        validMultipartFileWithLowercaseTitles = new MockMultipartFile("file", file2.getName(), null, new FileInputStream(file2));

        File file3 = new File("src/test/resources/invalidTestFile_1.xlsx");
        invalidMultipartFile = new MockMultipartFile("file", file3.getName(), null, new FileInputStream(file3));
    }

    @Test
    public void saveFileData_shouldReturnProcessedFileDto() {
        ProcessedFileDto processedFileDto = ProcessedFileDto.builder()
                .id(1L)
                .fileName(validMultipartFile.getOriginalFilename())
                .build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.of(Optional.of(processedFileDto)));
        Validation<String, ProcessedFileDto> validation = eventMessageService.saveFileData(validMultipartFile);
        assertTrue(validation.isValid());
        assertEquals(processedFileDto.getId(), validation.get().getId());
        assertEquals(processedFileDto.getFileName(), validation.get().getFileName());
    }

    @Test
    public void saveFileData_shouldReturnErrorMessage() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenThrow(new RuntimeException("Error message Test"));
        Validation<String, ProcessedFileDto> validation = eventMessageService.saveFileData(validMultipartFile);
        assertTrue(validation.isInvalid());
        assertEquals("Error message Test", validation.getError());
    }

    @Test
    public void deleteEventMessageById_shouldReturnProcessedFileDto() {
        ProcessedFileDto processedFileDto = ProcessedFileDto.builder()
                .id(1L)
                .fileName(validMultipartFile.getOriginalFilename())
                .build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class), anyLong()))
                .thenReturn(ResponseEntity.ok().build());
        Validation<String, Boolean> validation = eventMessageService.deleteEventMessageById(processedFileDto);
        assertTrue(validation.isValid());
    }

    @Test
    public void deleteEventMessageById_shouldReturnErrorMessage() {
        ProcessedFileDto processedFileDto = ProcessedFileDto.builder()
                .id(1L)
                .fileName(validMultipartFile.getOriginalFilename())
                .build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class), anyLong()))
                .thenThrow(new RuntimeException("Error message Test"));
        Validation<String, Boolean> validation = eventMessageService.deleteEventMessageById(processedFileDto);
        assertTrue(validation.isInvalid());
        assertEquals("Error message Test", validation.getError());
    }

}
