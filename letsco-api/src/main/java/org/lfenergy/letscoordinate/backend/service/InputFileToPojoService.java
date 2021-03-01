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

import io.vavr.control.Validation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.letscoordinate.backend.dto.ProcessedFileDto;
import org.lfenergy.letscoordinate.backend.dto.ResponseErrorDto;
import org.lfenergy.letscoordinate.backend.dto.ResponseErrorMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.enums.ResponseErrorSeverityEnum;
import org.lfenergy.letscoordinate.backend.exception.InvalidInputFileException;
import org.lfenergy.letscoordinate.backend.mapper.EventMessageMapper;
import org.lfenergy.letscoordinate.backend.model.EventMessage;
import org.lfenergy.letscoordinate.backend.processor.ExcelDataProcessor;
import org.lfenergy.letscoordinate.backend.processor.JsonDataProcessor;
import org.lfenergy.letscoordinate.backend.repository.EventMessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InputFileToPojoService {

    private final JsonDataProcessor jsonDataProcessor;
    private final ExcelDataProcessor excelDataProcessor;

    private final EventMessageRepository eventMessageRepository;

    private List<String> acceptedFileExtensions = Arrays.asList("json", "xlsx");

    /**
     * This methode allows to transform an uploaded {@link MultipartFile} to an {@link EventMessageDto} object.
     * If the uploaded file is not valid, this method returns a pojo of type {@link ResponseErrorDto} containing the error details.
     *
     * @param multipartFile
     * @return EventMessageDto object if the input file is valid, ResponseErrorDto object else
     */
    public Validation<ResponseErrorDto, EventMessageDto> uploadedFileToPojo(MultipartFile multipartFile) {
        try {
            String fileExtension = getFileExtension(multipartFile.getOriginalFilename().toLowerCase());
            if (fileExtension == null || !acceptedFileExtensions.contains(fileExtension))
                throw new RuntimeException("Invalid uploaded file type! Accepted types are: " + acceptedFileExtensions.toString());
            Validation<ResponseErrorDto, EventMessageDto> validation = null;
            switch (fileExtension) {
                case "json":
                    validation = jsonDataProcessor.inputStreamToPojo(multipartFile.getInputStream());
                    break;
                case "xlsx":
                    validation = excelDataProcessor.inputStreamToPojo(multipartFile.getOriginalFilename(), multipartFile.getInputStream());
                    break;
            }
            return validation;
        } catch (InvalidInputFileException ie) {
            log.error(ie.getMessage(), ie);
            return Validation.invalid(ResponseErrorDto.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .code("INVALID_INPUT_FILE") // TODO specific error code to be defined!
                    .messages(Collections.singletonList(ResponseErrorMessageDto.builder()
                            .severity(ResponseErrorSeverityEnum.ERROR)
                            .message("Error while generating POJO from uploaded file: Invalid input file \"" + multipartFile.getOriginalFilename() + "\"")
                            .detail(ie.getMessage())
                            .build()))
                    .build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Validation.invalid(ResponseErrorDto.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .code("ERROR") // TODO specific error code to be defined!
                    .messages(Collections.singletonList(ResponseErrorMessageDto.builder()
                            .severity(ResponseErrorSeverityEnum.ERROR)
                            .message("Error while generating POJO from uploaded file: " + multipartFile.getOriginalFilename())
                            .detail(e.getMessage())
                            .build()))
                    .build());
        }
    }

    public Validation<ResponseErrorDto, Map<String, EventMessageDto>> uploadedExcelsToPojo(MultipartFile[] multipartFiles) {
        Map<String, EventMessageDto> eventMessageDtoMap = new LinkedHashMap<>();
        for (MultipartFile multipartFile : multipartFiles) {
            Validation<ResponseErrorDto, EventMessageDto> validation = uploadedFileToPojo(multipartFile);
            if (validation.isInvalid())
                return Validation.invalid(validation.getError());
            eventMessageDtoMap.put(multipartFile.getOriginalFilename(), validation.get());
        }
        return Validation.valid(eventMessageDtoMap);
    }

    public Validation<ResponseErrorDto, EventMessageDto> excelToPojo(String filePath, String fileName) {
        String path = filePath + File.separator + fileName;
        log.info("Generating POJO for Excel file \"{}\"", path);
        try {
            return excelDataProcessor.inputStreamToPojo(path, new FileInputStream(new File(path)));
        } catch (InvalidInputFileException ie) {
            log.error(ie.getMessage(), ie);
            return Validation.invalid(ResponseErrorDto.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .code("INVALID_INPUT_FILE") // TODO specific error code to be defined!
                    .messages(Collections.singletonList(ResponseErrorMessageDto.builder()
                            .severity(ResponseErrorSeverityEnum.ERROR)
                            .message("Error while generating POJO from uploaded file: Invalid input file \"" + fileName + "\"")
                            .detail(ie.getMessage())
                            .build()))
                    .build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Validation.invalid(ResponseErrorDto.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .code("ERROR") // TODO specific error code to be defined!
                    .messages(Collections.singletonList(ResponseErrorMessageDto.builder()
                            .severity(ResponseErrorSeverityEnum.ERROR)
                            .message("Error while generating POJO from file " + path)
                            .detail(e.getMessage())
                            .build()))
                    .build());
        }
    }

    public Validation<ResponseErrorDto, EventMessage> uploadExcelFileAndSaveGeneratedData(MultipartFile multipartFile) {
        Validation<ResponseErrorDto, EventMessageDto> validation = uploadedFileToPojo(multipartFile);
        // the .map transformation is applied when the validation is valid, otherwise the invalid validation is returned as is
        return validation.map(valid -> eventMessageRepository.save(EventMessageMapper.fromDto(valid)));
    }

    public Validation<ResponseErrorDto, List<ProcessedFileDto>> uploadExcelFilesAndSaveGeneratedData(MultipartFile[] multipartFiles) {
        Validation<ResponseErrorDto, Map<String, EventMessageDto>> validations = uploadedExcelsToPojo(multipartFiles);
        // the .map transformation is applied when the validation is valid, otherwise the invalid validation is returned as is
        return validations.map(validList -> validList.entrySet().stream()
                .map(entry -> ProcessedFileDto.builder()
                        .id(eventMessageRepository.save(EventMessageMapper.fromDto(entry.getValue())).getId())
                        .fileName(entry.getKey())
                        .build()
                )
                .collect(Collectors.toList())
        );
    }

    public Validation<ResponseErrorDto, Boolean> deleteEventMessageById(long id) {
        try {
            log.info("Deleting EventMessage (id={})...", id);
            eventMessageRepository.deleteById(id);
            log.info("EventMessage record (id={}) deleted with success!", id);
            return Validation.valid(Boolean.TRUE);
        } catch (Exception e) {
            log.info("An error occurred while deleting EventMessage record (id={})!", id, e);
            return Validation.invalid(ResponseErrorDto.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .code("ERROR") // TODO specific error code to be defined!
                    .messages(Collections.singletonList(ResponseErrorMessageDto.builder()
                            .severity(ResponseErrorSeverityEnum.ERROR)
                            .message("Error while deleting EventMessage (id=" + id + ")")
                            .detail(e.getMessage())
                            .build()))
                    .build());
        }
    }

    public Validation<ResponseErrorDto, Boolean> deleteAllEventMessages() {
        try {
            log.info("Deleting all EventMessages");
            eventMessageRepository.deleteAll();
            log.info("All EventMessages deleted with success!");
            return Validation.valid(Boolean.TRUE);
        } catch (Exception e) {
            log.info("An error occurred while deleting all EventMessages", e);
            return Validation.invalid(ResponseErrorDto.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .code("ERROR") // TODO specific error code to be defined!
                    .messages(Collections.singletonList(ResponseErrorMessageDto.builder()
                            .severity(ResponseErrorSeverityEnum.ERROR)
                            .message("Error while deleting all EventMessages")
                            .detail(e.getMessage())
                            .build()))
                    .build());
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return null;
        String[] tokens = fileName.split("[.]");
        return tokens[tokens.length - 1];
    }

}
