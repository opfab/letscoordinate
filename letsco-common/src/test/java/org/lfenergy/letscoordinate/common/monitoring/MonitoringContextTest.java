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

package org.lfenergy.letscoordinate.common.monitoring;

import io.vavr.control.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.common.exception.AuthorizationException;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class MonitoringContextTest {

    @MockBean
    IMonitoringService monitoringService;

    @BeforeEach
    public void before() {

    }

    @Test
    public void startTaskMonitoring_taskEnum() {
        TaskEnum taskEnum = TaskEnum.EVENTMESSAGE_FTP_SCAN;
        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(taskEnum);
        assertAll(
                () -> assertNotNull(monitoringContext),
                () -> assertNotNull(monitoringContext.getMonitoredTask()),
                () -> assertEquals(taskEnum, monitoringContext.getMonitoredTask().getTask()),
                () -> assertNotNull(monitoringContext.getMonitoredTask().getUuid()),
                () -> assertNotNull(monitoringContext.getMonitoredTask().getStartTime()),
                () -> assertNotNull(monitoringContext.getMonitoredTask().getMonitoredTaskSteps()),
                () -> assertTrue(monitoringContext.getMonitoredTask().getMonitoredTaskSteps().isEmpty())
        );

    }

    @Test
    public void stopTaskMonitoring() {
        TaskEnum taskEnum = TaskEnum.EVENTMESSAGE_FTP_SCAN;
        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(taskEnum);
        monitoringContext.stopTaskMonitoring();
        assertAll(
                () -> assertNotNull(monitoringContext),
                () -> assertNotNull(monitoringContext.getMonitoredTask()),
                () -> assertNotNull(monitoringContext.getMonitoredTask().getEndTime()),
                () -> assertTrue(monitoringContext.getMonitoredTask().isTerminated())
        );

    }

    @Test
    public void startNewStepMonitoring_onlyStepParam() {
        TaskEnum taskEnum = TaskEnum.EVENTMESSAGE_FTP_SCAN;
        TaskStepEnum taskStepEnum = TaskStepEnum.STEP_DOWNLOAD_FILES;
        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(taskEnum);
        monitoringContext.startNewStepMonitoring(taskStepEnum);
        assertAll(
                () -> assertNotNull(monitoringContext),
                () -> assertNotNull(monitoringContext.getMonitoredTask()),
                () -> assertEquals(1, monitoringContext.getMonitoredTask().getMonitoredTaskSteps().size()),
                () -> assertEquals(1, monitoringContext.getMonitoredTask().getMonitoredTaskSteps().size()),
                () -> assertFalse(monitoringContext.getMonitoredTask().isTerminated())
        );

    }

    @Test
    public void stopCurrentStepMonitoring_OK() {
        TaskEnum taskEnum = TaskEnum.EVENTMESSAGE_FTP_SCAN;
        TaskStepEnum taskStepEnum = TaskStepEnum.STEP_DOWNLOAD_FILES;
        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(taskEnum);
        monitoringContext.startNewStepMonitoring(taskStepEnum);
        monitoringContext.stopCurrentStepMonitoring();
        assertAll(
                () -> assertNotNull(monitoringContext),
                () -> assertNotNull(monitoringContext.getMonitoredTask()),
                () -> assertEquals(1, monitoringContext.getMonitoredTask().getMonitoredTaskSteps().size()),
                () -> assertFalse(monitoringContext.getMonitoredTask().isTerminated()),
                () -> assertTrue(monitoringContext.currentMonitoredTaskStep.isTerminated()),
                () -> assertEquals(TaskStatusEnum.OK, monitoringContext.currentMonitoredTaskStep.getStatus()),
                () -> assertNull(monitoringContext.currentMonitoredTaskStep.getComment()),
                () -> assertNull(monitoringContext.currentMonitoredTaskStep.getCommentDetails())
        );
    }

    @Test
    public void stopCurrentStepMonitoring_ERROR() {
        TaskEnum taskEnum = TaskEnum.EVENTMESSAGE_FTP_SCAN;
        TaskStepEnum taskStepEnum = TaskStepEnum.STEP_DOWNLOAD_FILES;
        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(taskEnum);
        monitoringContext.startNewStepMonitoring(taskStepEnum);
        monitoringContext.stopCurrentStepMonitoring(TaskStatusEnum.ERROR, new AuthorizationException("Authorization exception message"));
        assertAll(
                () -> assertNotNull(monitoringContext),
                () -> assertNotNull(monitoringContext.getMonitoredTask()),
                () -> assertEquals(1, monitoringContext.getMonitoredTask().getMonitoredTaskSteps().size()),
                () -> assertFalse(monitoringContext.getMonitoredTask().isTerminated()),
                () -> assertTrue(monitoringContext.currentMonitoredTaskStep.isTerminated()),
                () -> assertEquals(TaskStatusEnum.ERROR, monitoringContext.currentMonitoredTaskStep.getStatus()),
                () -> assertEquals("Authorization exception message", monitoringContext.currentMonitoredTaskStep.getComment()),
                () -> assertNotNull(monitoringContext.currentMonitoredTaskStep.getCommentDetails())
        );
    }

    @Test
    public void stopCurrentStepMonitoring_WARN() {
        TaskEnum taskEnum = TaskEnum.EVENTMESSAGE_FTP_SCAN;
        TaskStepEnum taskStepEnum = TaskStepEnum.STEP_DOWNLOAD_FILES;
        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(taskEnum);
        monitoringContext.startNewStepMonitoring(taskStepEnum);
        monitoringContext.stopCurrentStepMonitoring(TaskStatusEnum.WARN, "warning message", "warning message details");
        assertAll(
                () -> assertNotNull(monitoringContext),
                () -> assertNotNull(monitoringContext.getMonitoredTask()),
                () -> assertEquals(1, monitoringContext.getMonitoredTask().getMonitoredTaskSteps().size()),
                () -> assertFalse(monitoringContext.getMonitoredTask().isTerminated()),
                () -> assertTrue(monitoringContext.currentMonitoredTaskStep.isTerminated()),
                () -> assertEquals(TaskStatusEnum.WARN, monitoringContext.currentMonitoredTaskStep.getStatus()),
                () -> assertEquals("warning message", monitoringContext.currentMonitoredTaskStep.getComment()),
                () -> assertEquals("warning message details", monitoringContext.currentMonitoredTaskStep.getCommentDetails())
        );
    }

    @Test
    public void getMonitoredTaskUUID() {
        TaskEnum taskEnum = TaskEnum.EVENTMESSAGE_FTP_SCAN;
        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(taskEnum);
        assertNotNull(monitoringContext.getMonitoredTaskUUID());
    }

    @Test
    public void saveMonitoredTask_monitoredTaskTerminated_savingSuccess() {
        when(monitoringService.saveMonitoredTask(any(MonitoredTask.class))).thenReturn(Validation.valid(1L));
        TaskEnum taskEnum = TaskEnum.EVENTMESSAGE_FTP_SCAN;
        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(taskEnum);
        monitoringContext.stopTaskMonitoring();
        monitoringContext.saveMonitoredTask(monitoringService);
    }

    @Test
    public void saveMonitoredTask_monitoredTaskTerminated_savingFailed() {
        when(monitoringService.saveMonitoredTask(any(MonitoredTask.class))).thenReturn(Validation.invalid("Error!"));
        TaskEnum taskEnum = TaskEnum.EVENTMESSAGE_FTP_SCAN;
        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(taskEnum);
        monitoringContext.stopTaskMonitoring();
        monitoringContext.saveMonitoredTask(monitoringService);
    }

    @Test
    public void saveMonitoredTask_monitoredTaskNotTerminated() {
        when(monitoringService.saveMonitoredTask(any(MonitoredTask.class))).thenReturn(Validation.invalid("Error!"));
        TaskEnum taskEnum = TaskEnum.EVENTMESSAGE_FTP_SCAN;
        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(taskEnum);
        monitoringContext.saveMonitoredTask(monitoringService);
    }

    @Test
    public void saveMonitoredTask_MonitoringServiceNull() {
        TaskEnum taskEnum = TaskEnum.EVENTMESSAGE_FTP_SCAN;
        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(taskEnum);
        monitoringContext.saveMonitoredTask(null);
    }

    @Test
    public void monitoredTaskStep() {
        MonitoredTaskStep monitoredTaskStep = new MonitoredTaskStep();
        monitoredTaskStep.setStep(TaskStepEnum.STEP_SAVE_FILE_DATA);
        monitoredTaskStep.setContext("context");
        monitoredTaskStep.setStartTime(LocalDateTime.now());

        assertFalse(monitoredTaskStep.isTerminated());

        monitoredTaskStep.setStatus(TaskStatusEnum.OK);

        assertFalse(monitoredTaskStep.isTerminated());

        monitoredTaskStep.setEndTime(LocalDateTime.now());

        assertTrue(monitoredTaskStep.isTerminated());
    }

}
