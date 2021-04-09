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

package org.lfenergy.letscoordinate.backend.service;

import io.vavr.control.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.backend.dto.ResponseErrorDto;
import org.lfenergy.letscoordinate.backend.dto.monitoring.MonitoredTaskDto;
import org.lfenergy.letscoordinate.backend.dto.monitoring.MonitoredTaskStepDto;
import org.lfenergy.letscoordinate.backend.model.MonitoredTask;
import org.lfenergy.letscoordinate.backend.repository.MonitoredTaskRepository;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MonitoringServiceTest {

    MonitoringService monitoringService;
    @Mock
    MonitoredTaskRepository monitoredTaskRepository;

    @BeforeEach
    public void before() {
        monitoringService = new MonitoringService(monitoredTaskRepository);
    }

    @Test
    public void saveMonitoredTask_shouldReturnMonitoredTask() {
        MonitoredTaskDto dto = MonitoredTaskDto.builder()
                .task("task")
                .uuid("uuid")
                .startTime(LocalDateTime.of(2019, 1, 15, 0, 0))
                .endTime(LocalDateTime.of(2020, 1, 15, 0, 0))
                .monitoredTaskSteps(Arrays.asList(MonitoredTaskStepDto.builder()
                        .step("step1")
                        .context("context1")
                        .startTime(LocalDateTime.of(2019, 2, 8, 0, 0))
                        .endTime(LocalDateTime.of(2019, 2, 9, 0, 0))
                        .status("status1")
                        .comment("comment1")
                        .commentDetails("commentDetails1")
                        .build()))
                .build();
        when(monitoredTaskRepository.save(any(MonitoredTask.class))).then(i -> i.getArgument(0, MonitoredTask.class));
        Validation<ResponseErrorDto, MonitoredTask> validation = monitoringService.saveMonitoredTask(dto);
        assertAll(
                () -> assertNotNull(validation),
                () -> assertTrue(validation.isValid()),
                () -> assertNull(validation.get().getId()),
                () -> assertEquals("task", validation.get().getTask()),
                () -> assertEquals("uuid", validation.get().getUuid()),
                () -> assertEquals(LocalDateTime.of(2019, 1, 15, 0, 0), validation.get().getStartTime()),
                () -> assertEquals(LocalDateTime.of(2020, 1, 15, 0, 0), validation.get().getEndTime()),
                () -> assertEquals(1, validation.get().getMonitoredTaskSteps().size()),
                () -> assertNotNull(validation.get().getMonitoredTaskSteps().get(0)),
                () -> assertEquals(null, validation.get().getMonitoredTaskSteps().get(0).getId()),
                () -> assertEquals("step1", validation.get().getMonitoredTaskSteps().get(0).getStep()),
                () -> assertEquals("context1", validation.get().getMonitoredTaskSteps().get(0).getContext()),
                () -> assertEquals(LocalDateTime.of(2019, 2, 8, 0, 0), validation.get().getMonitoredTaskSteps().get(0).getStartTime()),
                () -> assertEquals(LocalDateTime.of(2019, 2, 9, 0, 0), validation.get().getMonitoredTaskSteps().get(0).getEndTime()),
                () -> assertEquals("status1", validation.get().getMonitoredTaskSteps().get(0).getStatus()),
                () -> assertEquals("comment1", validation.get().getMonitoredTaskSteps().get(0).getComment()),
                () -> assertEquals("commentDetails1", validation.get().getMonitoredTaskSteps().get(0).getCommentDetails())
        );
    }

    @Test
    public void saveMonitoredTask_shouldReturnResponseErrorDto() {
        when(monitoredTaskRepository.save(any(MonitoredTask.class))).thenThrow(RuntimeException.class);
        Validation<ResponseErrorDto, MonitoredTask> validation = monitoringService.saveMonitoredTask(MonitoredTaskDto.builder().build());
        assertAll(
                () -> assertNotNull(validation),
                () -> assertTrue(validation.isInvalid()),
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), validation.getError().getStatus())
        );
    }

}
