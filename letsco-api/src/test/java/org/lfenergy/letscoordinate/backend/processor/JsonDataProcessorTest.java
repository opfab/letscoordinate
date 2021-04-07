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

package org.lfenergy.letscoordinate.backend.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vavr.control.Validation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.config.LetscoProperties;
import org.lfenergy.letscoordinate.backend.dto.ResponseErrorDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.service.EventMessageService;
import org.lfenergy.letscoordinate.backend.util.ApplicationContextUtil;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JsonDataProcessorTest {

    JsonDataProcessor jsonDataProcessor;
    ObjectMapper objectMapper;
    CoordinationConfig coordinationConfig;
    LetscoProperties letscoProperties;
    EventMessageService eventMessageService;

    @BeforeEach
    public void before() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        coordinationConfig = ApplicationContextUtil.initCoordinationConfig();
        letscoProperties = ApplicationContextUtil.initLetscoProperties();
        eventMessageService = new EventMessageService(coordinationConfig, letscoProperties);
        jsonDataProcessor = new JsonDataProcessor(objectMapper, eventMessageService);
    }

    @Test
    public void inputStreamToPojo_shouldReturnEventMessageDto() throws Exception {
        File file = new File("src/test/resources/validTestFile_1.json");
        InputStream inputStream = new FileInputStream(file);
        Validation<ResponseErrorDto, EventMessageDto> validation = jsonDataProcessor.inputStreamToPojo(inputStream);
        assertAll(
                () -> assertTrue(validation.isValid()),
                () -> assertNotNull(validation.get()),
                () -> assertEquals("ProcessSuccess", validation.get().getHeader().getNoun())
        );
        // TODO Test fields values one by one
    }

}
