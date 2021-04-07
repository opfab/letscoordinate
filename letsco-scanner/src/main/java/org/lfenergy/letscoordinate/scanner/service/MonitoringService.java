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

package org.lfenergy.letscoordinate.scanner.service;

import io.vavr.control.Validation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.letscoordinate.scanner.config.LetscoProperties;
import org.lfenergy.letscoordinate.common.monitoring.IMonitoringService;
import org.lfenergy.letscoordinate.common.monitoring.MonitoredTask;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService implements IMonitoringService {

    private final LetscoProperties letscoProperties;
    private final RestTemplate restTemplate;

    public Validation<String, Long> saveMonitoredTask(MonitoredTask monitoredTask) {
        String serverUrl = letscoProperties.getBackend().getBaseUrl() + "/letsco/api/v1/monitoring";
        try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity entity = new HttpEntity(monitoredTask, headers);
            log.info("Saving monitored task \"{}\" ...", monitoredTask.getUuid());
            ResponseEntity<Long> response = restTemplate.exchange(serverUrl, HttpMethod.POST, entity, Long.class);
            return Validation.valid(response.getBody());
        } catch (Exception e) {
            log.info("Error occurred during saving monitored task \"{}\"!", monitoredTask.getUuid(), e);
            return Validation.invalid(e.getMessage());
        }
    }

}
