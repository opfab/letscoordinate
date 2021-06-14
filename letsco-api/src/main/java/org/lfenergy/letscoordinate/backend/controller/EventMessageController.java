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
import io.vavr.control.Validation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.letscoordinate.backend.component.OpfabPublisherComponent;
import org.lfenergy.letscoordinate.backend.config.LetscoProperties;
import org.lfenergy.letscoordinate.backend.dto.ProcessedFileDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageWrapperDto;
import org.lfenergy.letscoordinate.backend.model.Coordination;
import org.lfenergy.letscoordinate.backend.service.CoordinationService;
import org.lfenergy.letscoordinate.backend.service.InputFileToPojoService;
import org.opfab.cards.model.Card;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity uploadedExcelToJson(@RequestParam("file") MultipartFile multipartFile) {
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
    public ResponseEntity uploadedExcelsToJson(@RequestBody MultipartFile[] multipartFiles) {
        return inputFileToPojoService.uploadedExcelsToPojo(multipartFiles).fold(
                invalid -> ResponseEntity.status(invalid.getStatus()).body(invalid),
                valid -> ResponseEntity.ok(valid)
        );
    }

    @PostMapping(value = "/upload/save", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @ApiOperation(value = "Validate uploaded file (Excel or JSON) and save validated data into database")
    @ApiImplicitParam(required = true, name = "Authorization", dataType = "string", paramType = "header")
    public ResponseEntity saveUploadedExcelFile(@RequestParam("file") MultipartFile multipartFile) {
        return inputFileToPojoService.uploadExcelFileAndSaveGeneratedData(multipartFile).fold(
                invalid -> ResponseEntity.status(invalid.getStatus()).body(invalid),
                valid -> ResponseEntity.ok(ProcessedFileDto.builder()
                        .id(valid.getId())
                        .fileName(multipartFile.getOriginalFilename())
                        .build()
                )
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
    public ResponseEntity saveUploadedExcelFiles(@RequestParam("file") MultipartFile[] multipartFiles) {
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

    @PostMapping("/coordination")
    @ApiOperation(value = "Coordination callback", hidden = true)
    public ResponseEntity coordinationCallback(@RequestBody Card card) {
        Validation<Boolean, Coordination> validation = coordinationService.saveAnswersAndCheckIfAllTsosHaveAnswered(card);
        if (validation.isValid()) {
            opfabPublisherComponent.publishOpfabCoordinationResultCard(validation.get());
        } else {
            log.debug("Some entities did not respond yet!");
        }
        return ResponseEntity.ok().build();
    }

}
