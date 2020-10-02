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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.letscoordinate.backend.dto.ResponseErrorDto;
import org.lfenergy.letscoordinate.backend.dto.monitoring.MonitoredTaskDto;
import org.lfenergy.letscoordinate.backend.mapper.MonitoringMapper;
import org.lfenergy.letscoordinate.backend.model.MonitoredTask;
import org.lfenergy.letscoordinate.backend.repository.MonitoredTaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MonitoringService {

    private final MonitoredTaskRepository monitoredTaskRepository;

    public Validation<ResponseErrorDto, MonitoredTask> saveMonitoredTask(MonitoredTaskDto monitoredTaskDto) {
        try {
            log.info("Saving monitored task \"{}\" ...", monitoredTaskDto.getUuid());
            return Validation.valid(monitoredTaskRepository.save(MonitoringMapper.fromDto(monitoredTaskDto)));
        } catch (Exception e) {
            log.error("Error occurred during saving monitored task \"{}\"!", monitoredTaskDto.getUuid(), e);
            return Validation.invalid(ResponseErrorDto.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .code("ERROR") // TODO specific error code to be defined!
                    .message("Error occurred during saving monitored task \"{}\"" + monitoredTaskDto.getUuid() + "!")
                    .detail(e.getMessage())
                    .build());
        }
    }
}
