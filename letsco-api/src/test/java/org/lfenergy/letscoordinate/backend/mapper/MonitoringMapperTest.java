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

package org.lfenergy.letscoordinate.backend.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.backend.dto.monitoring.MonitoredTaskDto;
import org.lfenergy.letscoordinate.backend.dto.monitoring.MonitoredTaskStepDto;
import org.lfenergy.letscoordinate.backend.model.MonitoredTask;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class MonitoringMapperTest {

    @Test
    public void fromDto_shouldReturnNullWhenInputMonitoredTaskDtoIsNull() {
        assertNull(MonitoringMapper.fromDto(null));
    }

    @Test
    public void fromDto_shouldReturnNullWhenInputMonitoredTaskDtoIsNotNull() {
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
        MonitoredTask entity = MonitoringMapper.fromDto(dto);
        assertAll(
                () -> assertNotNull(entity),
                () -> assertNull(entity.getId()),
                () -> assertEquals("task", entity.getTask()),
                () -> assertEquals("uuid", entity.getUuid()),
                () -> assertEquals(LocalDateTime.of(2019, 1, 15, 0, 0), entity.getStartTime()),
                () -> assertEquals(LocalDateTime.of(2020, 1, 15, 0, 0), entity.getEndTime()),
                () -> assertEquals(1, entity.getMonitoredTaskSteps().size()),
                () -> assertNotNull(entity.getMonitoredTaskSteps().get(0)),
                () -> assertEquals(null, entity.getMonitoredTaskSteps().get(0).getId()),
                () -> assertEquals("step1", entity.getMonitoredTaskSteps().get(0).getStep()),
                () -> assertEquals("context1", entity.getMonitoredTaskSteps().get(0).getContext()),
                () -> assertEquals(LocalDateTime.of(2019, 2, 8, 0, 0), entity.getMonitoredTaskSteps().get(0).getStartTime()),
                () -> assertEquals(LocalDateTime.of(2019, 2, 9, 0, 0), entity.getMonitoredTaskSteps().get(0).getEndTime()),
                () -> assertEquals("status1", entity.getMonitoredTaskSteps().get(0).getStatus()),
                () -> assertEquals("comment1", entity.getMonitoredTaskSteps().get(0).getComment()),
                () -> assertEquals("commentDetails1", entity.getMonitoredTaskSteps().get(0).getCommentDetails())
        );
    }

    @Test
    public void fromDto_shouldReturnNullWhenInputMonitoredTaskStepDtoIsNull() {
        assertNull(MonitoringMapper.fromDto(null, new MonitoredTask()));
    }

}
