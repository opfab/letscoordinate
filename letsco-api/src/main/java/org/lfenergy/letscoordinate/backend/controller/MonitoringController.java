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

package org.lfenergy.letscoordinate.backend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.lfenergy.letscoordinate.backend.dto.monitoring.MonitoredTaskDto;
import org.lfenergy.letscoordinate.backend.service.MonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/letsco/api/v1")
@RequiredArgsConstructor
@Api(description = "Controller providing APIs for scheduled tasks monitoring")
public class MonitoringController {

    final private MonitoringService monitoringService;

    @PostMapping(value = "/monitoring")
    @ApiOperation(value = "Save monitored task data into database")
    @ApiImplicitParam(required = true, name = "Authorization", dataType = "string", paramType = "header")
    public ResponseEntity saveMonitoredTask(@RequestBody MonitoredTaskDto monitoredTaskDto) {
        return monitoringService.saveMonitoredTask(monitoredTaskDto).fold(
                invalid -> ResponseEntity.status(invalid.getStatus()).body(invalid),
                valid -> ResponseEntity.ok(valid.getId())
        );
    }

}
