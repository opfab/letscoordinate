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

package org.lfenergy.letscoordinate.scanner.service;

import io.vavr.control.Validation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.letscoordinate.scanner.config.LetscoProperties;
import org.lfenergy.letscoordinate.scanner.dto.ProcessedFileDto;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventMessageService {

    private final LetscoProperties letscoProperties;

    public Validation<String, ProcessedFileDto> saveFileData(MultipartFile multipartFile) {
        String serverUrl = letscoProperties.getBackend().getBaseUrl() + "/letsco/api/v1/upload/save";
        RestTemplate restTemplate = new RestTemplate();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", multipartFile.getResource());
            HttpEntity entity = new HttpEntity(body, headers);

            log.info("Saving \"{}\" file's data into backend database ...", multipartFile.getOriginalFilename());
            ResponseEntity<ProcessedFileDto> response = restTemplate.exchange(serverUrl, HttpMethod.POST, entity, ProcessedFileDto.class);
            return Validation.valid(response.getBody());
        } catch (Exception e) {
            log.error("Error occurred during saving local file's data \"{}\" into backend database!", multipartFile.getOriginalFilename(), e);
            return Validation.invalid(e.getMessage());
        }
    }

    public Validation<String, Boolean> deleteEventMessageById(ProcessedFileDto processedFileDto) {
        String serverUrl = letscoProperties.getBackend().getBaseUrl() + "/letsco/api/v1/eventmessages/{id}";
        RestTemplate restTemplate = new RestTemplate();

        try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity entity = new HttpEntity(null, headers);

            log.info("Deleting \"{}\" file's data from backend database ...", processedFileDto.getFileName());
            ResponseEntity response = restTemplate.exchange(serverUrl, HttpMethod.DELETE, entity, Void.class, processedFileDto.getId());
            return Validation.valid(Boolean.TRUE);
        } catch (Exception e) {
            log.error("Error occurred during deleting \"{}\" file's data from backend database!", processedFileDto.getFileName(), e);
            return Validation.invalid(e.getMessage());
        }
    }

}
