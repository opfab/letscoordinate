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

package org.lfenergy.letscoordinate.backend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.letscoordinate.backend.component.OpfabPublisherComponent;
import org.lfenergy.letscoordinate.backend.config.LetscoProperties;
import org.lfenergy.letscoordinate.backend.dto.KafkaFileWrapperDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageWrapperDto;
import org.lfenergy.letscoordinate.backend.service.CoordinationService;
import org.lfenergy.letscoordinate.backend.service.InputFileToPojoService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.lfenergy.letscoordinate.backend.enums.FileDirectionEnum.INPUT;
import static org.lfenergy.letscoordinate.backend.enums.FileDirectionEnum.OUTPUT;

@RestController
@RequestMapping("/letsco/api/v1")
@RequiredArgsConstructor
@Api(description = "Controller providing APIs to manage XLSX and JSON EventMessage data")
@Slf4j
public class EventMessageController {

    final private LetscoProperties letscoProperties;
    final private InputFileToPojoService inputFileToPojoService;
    final private CoordinationService coordinationService;
    private final OpfabPublisherComponent opfabPublisherComponent;

    @PostMapping(value = "/upload/validate")
    @ApiOperation(value = "Validate uploaded file (Excel or JSON) and generate JSON from validated data")
    @ApiImplicitParam(required = true, name = "Authorization", dataType = "string", paramType = "header")
    public ResponseEntity validateFile(@RequestParam("file") MultipartFile multipartFile) {
        return inputFileToPojoService.uploadedFileToPojo(multipartFile).fold(
                invalid -> ResponseEntity.status(invalid.getStatus()).body(invalid),
                valid -> ResponseEntity.ok(EventMessageWrapperDto.builder().eventMessage(valid).build())
        );
    }

    @PostMapping(value = "/upload/validate-all", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @ApiOperation(
            value = "Validate uploaded files (Excel or JSON) and generate JSON from validated data",
            notes = "PLEASE NOTE:\nThis API is not testable with this version of Swagger-ui until the #2617 issue is fixed!\n" +
                    "MultipartFile[] doesn't show correctly: https://github.com/swagger-api/swagger-ui/issues/2617",
            hidden = true
    )
    @ApiImplicitParam(required = true, name = "Authorization", dataType = "string", paramType = "header")
    public ResponseEntity validateFiles(@RequestBody MultipartFile[] multipartFiles) {
        return inputFileToPojoService.uploadedExcelsToPojo(multipartFiles).fold(
                invalid -> ResponseEntity.status(invalid.getStatus()).body(invalid),
                valid -> ResponseEntity.ok(valid)
        );
    }

    @PostMapping(value = "/upload/save", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @ApiOperation(value = "Validate uploaded file (Excel or JSON) and save validated data into database (The file saving is asynchronous)")
    @ApiImplicitParam(required = true, name = "Authorization", dataType = "string", paramType = "header")
    public ResponseEntity saveUploadedFile(@RequestParam("file") MultipartFile multipartFile) {
        return inputFileToPojoService.uploadFileAndSaveGeneratedData(multipartFile).fold(
                invalid -> ResponseEntity.status(invalid.getStatus()).body(invalid),
                valid -> ResponseEntity.ok().build()
        );
    }

    @PostMapping(value = "/upload/save-all", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @ApiOperation(
            value = "Validate uploaded files (Excel or JSON) and save validated data into database",
            notes = "PLEASE NOTE:\nThis API is not testable with this version of Swagger-ui until the #2617 issue is fixed!\n" +
                    "MultipartFile[] doesn't show correctly: https://github.com/swagger-api/swagger-ui/issues/2617",
            hidden = true
    )
    @ApiImplicitParam(required = true, name = "Authorization", dataType = "string", paramType = "header")
    public ResponseEntity saveUploadedFiles(@RequestParam("file") MultipartFile[] multipartFiles) {
        return inputFileToPojoService.uploadExcelFilesAndSaveGeneratedData(multipartFiles).fold(
                invalid -> ResponseEntity.status(invalid.getStatus()).body(invalid),
                valid -> ResponseEntity.ok(valid)
        );
    }

    @DeleteMapping("/eventmessages/{id}")
    @ApiOperation(value = "Delete event_message data (with its dependencies) by id")
    @ApiImplicitParam(required = true, name = "Authorization", dataType = "string", paramType = "header")
    public ResponseEntity deleteEventMessageById(@PathVariable long id) {
        return inputFileToPojoService.deleteEventMessageById(id).fold(
                invalid -> ResponseEntity.status(invalid.getStatus()).body(invalid),
                valid -> ResponseEntity.ok().build()
        );
    }

    @DeleteMapping("/eventmessages")
    @ApiOperation(value = "Delete all event_message data (with their dependencies)")
    @ApiImplicitParam(required = true, name = "Authorization", dataType = "string", paramType = "header")
    public ResponseEntity deleteAllEventMessages() {
        return inputFileToPojoService.deleteAllEventMessages().fold(
                invalid -> ResponseEntity.status(invalid.getStatus()).body(invalid),
                valid -> ResponseEntity.ok().build()
        );
    }

    @GetMapping("/eventmessages/{id}/files/input")
    @ApiOperation(value = "Get input file by event_message id")
    @ApiImplicitParam(required = true, name = "Authorization", dataType = "string", paramType = "header")
    public ResponseEntity getInputFile(@PathVariable Long id) {
        return coordinationService.getEventMessageFileIfExists(id, INPUT)
                .map(eventMessageFile -> ResponseEntity.ok(KafkaFileWrapperDto.builder()
                        .fileName(eventMessageFile.getFileName())
                        .fileType(eventMessageFile.getFileType())
                        .fileContent(eventMessageFile.getFileContent())
                        .build()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/eventmessages/{id}/files/output")
    @ApiOperation(value = "Get output file by event_message id")
    @ApiImplicitParam(required = true, name = "Authorization", dataType = "string", paramType = "header")
    public ResponseEntity getOutputFile(@PathVariable Long id) {
        return coordinationService.getEventMessageFileIfExists(id, OUTPUT)
                .map(eventMessageFile -> ResponseEntity.ok(KafkaFileWrapperDto.builder()
                        .fileName(eventMessageFile.getFileName())
                        .fileType(eventMessageFile.getFileType())
                        .fileContent(eventMessageFile.getFileContent())
                        .build()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/eventmessages/{id}/generate-output-file")
    @ApiOperation(
            value = "Manual generation of output file for event_message by its id",
            notes = "For test purposes only!",
            hidden = true
    )
    @ApiImplicitParam(required = true, name = "Authorization", dataType = "string", paramType = "header")
    public ResponseEntity generateOutputFile(@PathVariable Long id) throws IOException {
        coordinationService.applyCoordinationAnswersToEventMessage(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/eventmessages/{id}/download-output-file")
    @ApiOperation(
            value = "Generate download link to download output file by event_message id",
            notes = "For test purposes only!",
            hidden = true
    )
    @ApiImplicitParam(required = true, name = "Authorization", dataType = "string", paramType = "header")
    public ResponseEntity downloadOutputFile(@PathVariable Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return coordinationService.getEventMessageFileIfExists(id, OUTPUT)
                .map(eventMessageFile -> {
                    headers.setContentDisposition(ContentDisposition.builder("inline")
                            .filename(eventMessageFile.getFileName())
                            .build());
                    return ResponseEntity.ok()
                            .headers(headers)
                            //.contentLength(eventMessageFile.getFileContent().length)
                            .contentType(MediaType.parseMediaType("application/octet-stream"))
                            .body(new ByteArrayResource(eventMessageFile.getFileContent()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

}
