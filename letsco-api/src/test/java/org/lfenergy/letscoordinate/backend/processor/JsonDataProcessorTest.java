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
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageWrapperDto;
import org.lfenergy.letscoordinate.backend.service.EventMessageService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class JsonDataProcessorTest {

    @InjectMocks
    JsonDataProcessor jsonDataProcessor;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    EventMessageService eventMessageService;

    @Test
    public void should_create_eventmessage_dto_from_valid_json_file() throws Exception {
        File file = new File("src/generateRscKpiExcelReport/resources/ValidJsonTestFile_1.json");
        InputStream inputStream = new FileInputStream(file);

        when(objectMapper.readValue(any(InputStream.class), any(Class.class)))
                .thenReturn(new ObjectMapper().registerModule(new JavaTimeModule())
                        .readValue(inputStream, EventMessageWrapperDto.class));

        EventMessageDto eventMessageDto = jsonDataProcessor.inputStreamToPojo(inputStream);
        Assertions.assertThat(eventMessageDto).isNotNull();
        Assertions.assertThat(eventMessageDto.getHeader().getNoun()).isEqualTo("ProcessSuccess");
        // TODO Test fields values one by one
    }

}
