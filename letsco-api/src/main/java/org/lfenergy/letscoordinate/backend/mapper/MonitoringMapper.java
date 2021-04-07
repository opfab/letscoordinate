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

package org.lfenergy.letscoordinate.backend.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.lfenergy.letscoordinate.backend.dto.monitoring.MonitoredTaskDto;
import org.lfenergy.letscoordinate.backend.dto.monitoring.MonitoredTaskStepDto;
import org.lfenergy.letscoordinate.backend.model.MonitoredTask;
import org.lfenergy.letscoordinate.backend.model.MonitoredTaskStep;

import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MonitoringMapper {

    public static MonitoredTask fromDto(MonitoredTaskDto monitoredTaskDto) {
        if (monitoredTaskDto == null)
            return null;

        MonitoredTask monitoredTask = new MonitoredTask();
        monitoredTask.setId(null);
        monitoredTask.setTask(monitoredTaskDto.getTask());
        monitoredTask.setUuid(monitoredTaskDto.getUuid());
        monitoredTask.setStartTime(monitoredTaskDto.getStartTime());
        monitoredTask.setEndTime(monitoredTaskDto.getEndTime());
        monitoredTask.setMonitoredTaskSteps(Optional.ofNullable(monitoredTaskDto.getMonitoredTaskSteps())
                .map(monitoredTaskStepDtos -> monitoredTaskStepDtos.stream()
                        .map(monitoredTaskStepDto -> MonitoringMapper.fromDto(monitoredTaskStepDto, monitoredTask))
                        .collect(Collectors.toList()))
                .orElse(null));

        return monitoredTask;
    }

    protected static MonitoredTaskStep fromDto(MonitoredTaskStepDto monitoredTaskStepDto, MonitoredTask monitoredTask) {
        if (monitoredTaskStepDto == null)
            return null;

        return MonitoredTaskStep.builder()
                .id(null)
                .monitoredTask(monitoredTask)
                .step(monitoredTaskStepDto.getStep())
                .context(monitoredTaskStepDto.getContext())
                .startTime(monitoredTaskStepDto.getStartTime())
                .endTime(monitoredTaskStepDto.getEndTime())
                .status(monitoredTaskStepDto.getStatus())
                .comment(monitoredTaskStepDto.getComment())
                .commentDetails(monitoredTaskStepDto.getCommentDetails())
                .build();
    }

}
