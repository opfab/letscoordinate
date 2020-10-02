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
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lfenergy.letscoordinate.backend.config.LetscoProperties;
import org.lfenergy.letscoordinate.backend.dto.ResponseErrorDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.processor.ExcelDataProcessor;
import org.lfenergy.letscoordinate.backend.util.ApplicationContextUtil;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

@RunWith(SpringRunner.class)
public class InputFileToPojoServiceTest {

    @InjectMocks
    InputFileToPojoService inputFileToPojoService;

    @Spy
    LetscoProperties letscoProperties = ApplicationContextUtil.initLetscoProperties();

    @Mock
    EventMessageService eventMessageService;

    @Spy
    MockExcelDataProcessor mockExcelDataProcessor;

    abstract class MockExcelDataProcessor extends ExcelDataProcessor {
        MockExcelDataProcessor() {
            super(letscoProperties, eventMessageService);
        }
    }

    @Test
    public void should_create_eventmessage_when_local_file_ok() {
        Validation<ResponseErrorDto, EventMessageDto> validation = inputFileToPojoService.excelToPojo("src/test/resources", "ValidTestFile_1.xlsx");
        Assertions.assertThat(validation).isNotNull();
        Assertions.assertThat(validation.isValid()).isTrue();
        Assertions.assertThat(validation.get().getHeader().getNoun()).isEqualTo("ServiceA_CalculationResults_1");
        // TODO Test fields values one by one
    }

    @Test
    public void should_create_eventmessage_when_uploaded_file_ok() throws Exception {
        File file = new File("src/test/resources/ValidTestFile_1.xlsx");
        MockMultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(), null, new FileInputStream(file));
        Validation<ResponseErrorDto, EventMessageDto> validation = inputFileToPojoService.uploadedFileToPojo(multipartFile);
        Assertions.assertThat(validation).isNotNull();
        Assertions.assertThat(validation.isValid()).isTrue();
        Assertions.assertThat(validation.get().getHeader().getNoun()).isEqualTo("ServiceA_CalculationResults_1");
        // TODO Test fields values one by one
    }

    @Test
    public void should_create_eventmessage_list_when_uploaded_files_ok() throws IOException {
        File file1 = new File("src/test/resources/ValidTestFile_1.xlsx");
        File file2 = new File("src/test/resources/ValidTestFile_2.xlsx");
        MultipartFile[] multipartFiles = {
                new MockMultipartFile(file1.getName(), file1.getName(), null, new FileInputStream(file1)),
                new MockMultipartFile(file2.getName(), file2.getName(), null, new FileInputStream(file2))
        };
        Validation<ResponseErrorDto, Map<String, EventMessageDto>> validation = inputFileToPojoService.uploadedExcelsToPojo(multipartFiles);
        Assertions.assertThat(validation).isNotNull();
        Assertions.assertThat(validation.isValid()).isTrue();
        Assertions.assertThat(validation.get().size()).isEqualTo(2);
        Assertions.assertThat(validation.get().get("ValidTestFile_1.xlsx").getHeader().getNoun()).isEqualTo("ServiceA_CalculationResults_1");
        Assertions.assertThat(validation.get().get("ValidTestFile_2.xlsx").getHeader().getNoun()).isEqualTo("ServiceA_CalculationResults_2");
    }

}
