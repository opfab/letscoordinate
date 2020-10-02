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

package org.lfenergy.letscoordinate.scanner.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.letscoordinate.scanner.client.FTPDownloadClient;
import org.lfenergy.letscoordinate.common.monitoring.MonitoringContext;
import org.lfenergy.letscoordinate.common.monitoring.TaskEnum;
import org.lfenergy.letscoordinate.scanner.service.MonitoringService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class Scheduler {

    private final FTPDownloadClient ftpDownloadClient;
    private final MonitoringService monitoringService;

    @Scheduled(cron = "#{letscoProperties.getScheduler().getCron().getDownloadFilesCron()}")
    public void downloadFtpFilesAndSaveIntoLetscoDB() {
        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(TaskEnum.EVENTMESSAGE_FTP_SCAN);
        log.info(">>> START PROCESS: downloadFtpFilesAndSaveIntoLetscoDB [{}]", monitoringContext.getMonitoredTaskUUID());
        List<MultipartFile> downloadedMultipartFiles = ftpDownloadClient.checkAndDownloadFtpFiles(monitoringContext);
        ftpDownloadClient.checkAndSaveFilesData(downloadedMultipartFiles, monitoringContext);
        monitoringContext.stopTaskMonitoring();
        monitoringContext.saveMonitoredTask(monitoringService);
        log.info("<<< END PROCESS: downloadFtpFilesAndSaveIntoLetscoDB [{}]", monitoringContext.getMonitoredTaskUUID());

    }

}
