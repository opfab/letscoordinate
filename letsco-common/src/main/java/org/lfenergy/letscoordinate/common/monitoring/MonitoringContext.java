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

package org.lfenergy.letscoordinate.common.monitoring;

import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MonitoringContext {

    @Getter
    private MonitoredTask monitoredTask;
    protected MonitoredTaskStep currentMonitoredTaskStep;

    public static MonitoringContext startTaskMonitoring(TaskEnum task) {
        MonitoringContext monitoringContext = new MonitoringContext();
        monitoringContext.monitoredTask = new MonitoredTask();
        monitoringContext.monitoredTask.setTask(task);
        monitoringContext.monitoredTask.setUuid(UUID.randomUUID().toString());
        monitoringContext.monitoredTask.setStartTime(LocalDateTime.now(ZoneId.of("UTC")));
        monitoringContext.monitoredTask.setMonitoredTaskSteps(new ArrayList<>());
        return monitoringContext;
    }

    public void stopTaskMonitoring() {
        stopCurrentStepMonitoring();
        monitoredTask.setEndTime(LocalDateTime.now(ZoneId.of("UTC")));
        traceMonitoringRecap();
    }

    public void startNewStepMonitoring(TaskStepEnum step) {
        startNewStepMonitoring(step, null);
    }

    public void startNewStepMonitoring(TaskStepEnum step,
                                       String stepSubject) {
        stopCurrentStepMonitoring();
        currentMonitoredTaskStep = initNewMonitoredStep(step, stepSubject);
        monitoredTask.getMonitoredTaskSteps().add(currentMonitoredTaskStep);
    }

    public void stopCurrentStepMonitoring() {
        stopCurrentStepMonitoring(TaskStatusEnum.OK, null, null);
    }

    public void stopCurrentStepMonitoring(TaskStatusEnum status,
                                          Throwable throwable) {
        stopCurrentStepMonitoring(status, throwable.getMessage(), Stream.of(throwable.getStackTrace()).collect(Collectors.toList()).toString());
    }

    public void stopCurrentStepMonitoring(TaskStatusEnum status,
                                          String comment,
                                          String commentDetails) {
        if(currentMonitoredTaskStep != null && !currentMonitoredTaskStep.isTerminated()) {
            currentMonitoredTaskStep.setStatus(status);
            currentMonitoredTaskStep.setEndTime(LocalDateTime.now(ZoneId.of("UTC")));
            currentMonitoredTaskStep.setComment(comment);
            currentMonitoredTaskStep.setCommentDetails(commentDetails);
        }
    }

    public String getMonitoredTaskUUID() {
        return monitoredTask.getUuid();
    }

    public void saveMonitoredTask(IMonitoringService monitoringService) {
        if(monitoringService == null) return;
        if(monitoredTask.isTerminated()) {
            Validation<String, Long> validation = monitoringService.saveMonitoredTask(monitoredTask);
            if(validation.isValid())
                log.info("Monitored task \"{}\" saved with success! >>> (id={})", getMonitoredTaskUUID(), validation.get());
            else
                log.info("Error occurred during saving monitored task \"{}\"!", getMonitoredTaskUUID());
        } else {
            log.warn("Monitored task \"{}\" is not finished yet! Saving monitored task is not allowed at this stage.", getMonitoredTaskUUID());
        }
    }

    // PRIVATE METHODES

    private MonitoredTaskStep initNewMonitoredStep(TaskStepEnum step,
                                                   String stepSubject) {
        MonitoredTaskStep monitoredTaskStep = new MonitoredTaskStep();
        monitoredTaskStep.setStep(step);
        monitoredTaskStep.setContext(stepSubject);
        monitoredTaskStep.setStartTime(LocalDateTime.now(ZoneId.of("UTC")));
        return monitoredTaskStep;
    }

    private void traceMonitoringRecap() {
        log.trace("{}", monitoredTask);
    }

}
