/*
 * Copyright (c) 2018-2020, RTE (https://www.rte-france.com)
 * Copyright (c) 2019-2020 RTE international (https://www.rte-international.com)
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.config.LetscoProperties;
import org.lfenergy.letscoordinate.backend.dto.ProcessedFileDto;
import org.lfenergy.letscoordinate.backend.dto.ResponseErrorDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.model.EventMessage;
import org.lfenergy.letscoordinate.backend.processor.ExcelDataProcessor;
import org.lfenergy.letscoordinate.backend.processor.JsonDataProcessor;
import org.lfenergy.letscoordinate.backend.repository.EventMessageRepository;
import org.lfenergy.letscoordinate.backend.util.ApplicationContextUtil;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InputFileToPojoServiceTest {

    InputFileToPojoService inputFileToPojoService;
    JsonDataProcessor jsonDataProcessor;
    ExcelDataProcessor excelDataProcessor;
    EventMessageRepository eventMessageRepository;
    LetscoProperties letscoProperties;
    CoordinationConfig coordinationConfig;
    EventMessageService eventMessageService;
    ObjectMapper objectMapper;

    @BeforeEach
    public void before() {
        eventMessageRepository = Mockito.mock(EventMessageRepository.class);

        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        coordinationConfig = ApplicationContextUtil.initCoordinationConfig();
        letscoProperties = ApplicationContextUtil.initLetscoProperties();
        eventMessageService = new EventMessageService(coordinationConfig, letscoProperties);
        jsonDataProcessor = new JsonDataProcessor(objectMapper, eventMessageService);
        excelDataProcessor = new ExcelDataProcessor(letscoProperties, coordinationConfig, eventMessageService);
        inputFileToPojoService = new InputFileToPojoService(jsonDataProcessor, excelDataProcessor, eventMessageRepository, null, letscoProperties);
    }

    @Test
    public void uploadedFileToPojo_shouldCreateEventMessageDto_xlsx() throws Exception {
        File file = new File("src/test/resources/validTestFile_1.xlsx");
        MockMultipartFile multipartFile = new MockMultipartFile("file", file.getName(), null, new FileInputStream(file));
        Validation<ResponseErrorDto, EventMessageDto> validation = inputFileToPojoService.uploadedFileToPojo(multipartFile);
        assertAll(
                () -> assertNotNull(validation),
                () -> assertTrue(validation.isValid()),
                () -> assertEquals("ServiceA_CalculationResults_1", validation.get().getHeader().getNoun())
        );
    }

    @Test
    public void uploadedFileToPojo_shouldCreateEventMessageDto_json() throws Exception {
        File file = new File("src/test/resources/validTestFile_1.json");
        MockMultipartFile multipartFile = new MockMultipartFile("file", file.getName(), null, new FileInputStream(file));
        Validation<ResponseErrorDto, EventMessageDto> validation = inputFileToPojoService.uploadedFileToPojo(multipartFile);
        assertAll(
                () -> assertNotNull(validation),
                () -> assertTrue(validation.isValid()),
                () -> assertEquals("ProcessSuccess", validation.get().getHeader().getNoun())
        );
    }

    @Test
    public void uploadedFileToPojo_shouldReturnResponseErrorDto_fileExtensionKO() throws Exception {
        File file = new File("src/test/resources/validTestFile_1.xlsx");
        MockMultipartFile multipartFile = new MockMultipartFile("file", null , null, new FileInputStream(file));
        Validation<ResponseErrorDto, EventMessageDto> validation = inputFileToPojoService.uploadedFileToPojo(multipartFile);
        assertAll(
                () -> assertNotNull(validation),
                () -> assertTrue(validation.isInvalid()),
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), validation.getError().getStatus()),
                () -> assertEquals("ERROR", validation.getError().getCode())
        );

        multipartFile = new MockMultipartFile("file", "file.txt" , null, new FileInputStream(file));
        Validation<ResponseErrorDto, EventMessageDto> validation2 = inputFileToPojoService.uploadedFileToPojo(multipartFile);
        assertAll(
                () -> assertNotNull(validation2),
                () -> assertTrue(validation2.isInvalid()),
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), validation2.getError().getStatus()),
                () -> assertEquals("ERROR", validation2.getError().getCode())
        );
    }

    @Test
    public void uploadedFileToPojo_shouldReturnResponseErrorDto_jsonFileValidation_KO() throws Exception {
        File file = new File("src/test/resources/invalidTestFile_1.xlsx");
        MockMultipartFile multipartFile = new MockMultipartFile("file", file.getName() , null, new FileInputStream(file));
        Validation<ResponseErrorDto, EventMessageDto> validation = inputFileToPojoService.uploadedFileToPojo(multipartFile);
        assertAll(
                () -> assertNotNull(validation),
                () -> assertTrue(validation.isInvalid()),
                () -> assertEquals(HttpStatus.BAD_REQUEST.value(), validation.getError().getStatus()),
                () -> assertEquals("INVALID_INPUT_FILE", validation.getError().getCode())
        );
    }

    @Test
    public void uploadedExcelsToPojo_shouldReturnEventMessageDtos() throws IOException {
        File file1 = new File("src/test/resources/validTestFile_1.xlsx");
        File file2 = new File("src/test/resources/validTestFile_2.xlsx");
        MultipartFile[] multipartFiles = {
                new MockMultipartFile("file", file1.getName(), null, new FileInputStream(file1)),
                new MockMultipartFile("file", file2.getName(), null, new FileInputStream(file2))
        };
        Validation<ResponseErrorDto, Map<String, EventMessageDto>> validation = inputFileToPojoService.uploadedExcelsToPojo(multipartFiles);
        assertAll(
                () -> assertNotNull(validation),
                () -> assertTrue(validation.isValid()),
                () -> assertEquals(2, validation.get().size()),
                () -> assertEquals("ServiceA_CalculationResults_1", validation.get().get("validTestFile_1.xlsx").getHeader().getNoun()),
                () -> assertEquals("ServiceA_CalculationResults_2", validation.get().get("validTestFile_2.xlsx").getHeader().getNoun())
        );
    }

    @Test
    public void uploadedExcelsToPojo_shouldReturnResponseErrorDto() throws IOException {
        File file1 = new File("src/test/resources/validTestFile_1.xlsx");
        File file2 = new File("src/test/resources/invalidTestFile_1.xlsx");
        MultipartFile[] multipartFiles = {
                new MockMultipartFile("file", file1.getName(), null, new FileInputStream(file1)),
                new MockMultipartFile("file", file2.getName(), null, new FileInputStream(file2))
        };
        Validation<ResponseErrorDto, Map<String, EventMessageDto>> validation = inputFileToPojoService.uploadedExcelsToPojo(multipartFiles);
        assertAll(
                () -> assertNotNull(validation),
                () -> assertTrue(validation.isInvalid())
        );
    }

    @Test
    public void uploadExcelFileAndSaveGeneratedData_validFile_shouldReturnSavedEventMessage() throws IOException {
        File file = new File("src/test/resources/validTestFile_1.xlsx");
        MockMultipartFile multipartFile = new MockMultipartFile("file", file.getName(), null, new FileInputStream(file));
        Validation<ResponseErrorDto, EventMessageDto> validation = inputFileToPojoService.uploadFileAndSaveGeneratedData(multipartFile);
        assertAll(
                () -> assertNotNull(validation),
                () -> assertTrue(validation.isValid())
        );
    }

    @Test
    public void uploadExcelFileAndSaveGeneratedData_invalidFile_shouldReturnResponseErrorDto() throws IOException {
        File file = new File("src/test/resources/invalidTestFile_1.xlsx");
        MockMultipartFile multipartFile = new MockMultipartFile("file", file.getName(), null, new FileInputStream(file));
        Validation<ResponseErrorDto, EventMessageDto> validation = inputFileToPojoService.uploadFileAndSaveGeneratedData(multipartFile);
        assertAll(
                () -> assertNotNull(validation),
                () -> assertTrue(validation.isInvalid()),
                () -> assertNotNull(validation.getError())
        );
    }

    @Test
    public void uploadExcelFilesAndSaveGeneratedData_validFile_shouldReturnSavedEventMessage() throws IOException {
        File file1 = new File("src/test/resources/validTestFile_1.xlsx");
        File file2 = new File("src/test/resources/validTestFile_2.xlsx");
        MultipartFile[] multipartFiles = {
                new MockMultipartFile("file", file1.getName(), null, new FileInputStream(file1)),
                new MockMultipartFile("file", file2.getName(), null, new FileInputStream(file2))
        };
        when(eventMessageRepository.save(any(EventMessage.class))).thenReturn(EventMessage.builder().build());

        File file = new File("src/test/resources/validTestFile_1.xlsx");
        MockMultipartFile multipartFile = new MockMultipartFile("file", file.getName(), null, new FileInputStream(file));
        Validation<ResponseErrorDto, List<ProcessedFileDto>> validation = inputFileToPojoService.uploadExcelFilesAndSaveGeneratedData(multipartFiles);
        assertAll(
                () -> assertNotNull(validation),
                () -> assertTrue(validation.isValid()),
                () -> assertEquals(2, validation.get().size())
        );
    }

    @Test
    public void uploadExcelFilesAndSaveGeneratedData_invalidFile_shouldReturnResponseErrorDto() throws IOException {
        File file1 = new File("src/test/resources/validTestFile_1.xlsx");
        File file2 = new File("src/test/resources/invalidTestFile_1.xlsx");
        MultipartFile[] multipartFiles = {
                new MockMultipartFile("file", file1.getName(), null, new FileInputStream(file1)),
                new MockMultipartFile("file", file2.getName(), null, new FileInputStream(file2))
        };
        Validation<ResponseErrorDto, List<ProcessedFileDto>> validation = inputFileToPojoService.uploadExcelFilesAndSaveGeneratedData(multipartFiles);
        assertAll(
                () -> assertNotNull(validation),
                () -> assertTrue(validation.isInvalid()),
                () -> assertNotNull(validation.getError())
        );
    }

    @Test
    public void deleteEventMessageById_shouldReturnValid() {
        Validation<ResponseErrorDto, Boolean> validation = inputFileToPojoService.deleteEventMessageById(1L);
        assertAll(
                () -> assertNotNull(validation),
                () -> assertTrue(validation.isValid()),
                () -> assertEquals(Boolean.TRUE, validation.get())
        );
    }

    @Test
    public void deleteEventMessageById_shouldReturnResponseErrorDto() {
        doThrow(Exception.class).when(eventMessageRepository);
        Validation<ResponseErrorDto, Boolean> validation = inputFileToPojoService.deleteEventMessageById(1L);
        assertAll(
                () -> assertNotNull(validation),
                () -> assertTrue(validation.isInvalid()),
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), validation.getError().getStatus()),
                () -> assertEquals("ERROR", validation.getError().getCode())
        );
    }

    @Test
    public void deleteAllEventMessages_shouldReturnValid() {
        Validation<ResponseErrorDto, Boolean> validation = inputFileToPojoService.deleteAllEventMessages();
        assertAll(
                () -> assertNotNull(validation),
                () -> assertTrue(validation.isValid()),
                () -> assertEquals(Boolean.TRUE, validation.get())
        );
    }

    @Test
    public void deleteAllEventMessages_shouldReturnResponseErrorDto() {
        doThrow(Exception.class).when(eventMessageRepository);
        Validation<ResponseErrorDto, Boolean> validation = inputFileToPojoService.deleteAllEventMessages();
        assertAll(
                () -> assertNotNull(validation),
                () -> assertTrue(validation.isInvalid()),
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), validation.getError().getStatus()),
                () -> assertEquals("ERROR", validation.getError().getCode())
        );
    }

}
