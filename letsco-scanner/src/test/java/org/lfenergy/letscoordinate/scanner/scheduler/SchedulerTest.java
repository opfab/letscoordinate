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

package org.lfenergy.letscoordinate.scanner.scheduler;

import io.vavr.control.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.common.exception.AuthorizationException;
import org.lfenergy.letscoordinate.common.monitoring.*;
import org.lfenergy.letscoordinate.scanner.client.FTPDownloadClient;
import org.lfenergy.letscoordinate.scanner.dto.ProcessedFileDto;
import org.lfenergy.letscoordinate.scanner.service.MonitoringService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class SchedulerTest {

    Scheduler scheduler;
    @MockBean
    FTPDownloadClient ftpDownloadClient;
    @MockBean
    MonitoringService monitoringService;

    @BeforeEach
    public void before() {
        scheduler = new Scheduler(ftpDownloadClient, monitoringService);
    }

    @Test
    public void downloadFtpFilesAndSaveIntoLetscoDB() {
        when(ftpDownloadClient.checkAndDownloadFtpFiles(any(MonitoringContext.class))).thenReturn(new ArrayList<>());
        doNothing().when(ftpDownloadClient).checkAndSaveFilesData(anyList(), any(MonitoringContext.class));
        when(monitoringService.saveMonitoredTask(any(MonitoredTask.class))).thenReturn(Validation.valid(1L));
        scheduler.downloadFtpFilesAndSaveIntoLetscoDB();
    }

    @Test
    public void processedFileDto() {
        ProcessedFileDto processedFileDto = new ProcessedFileDto();
        processedFileDto.setId(1L);
        processedFileDto.setFileName("fileName");
    }

}
