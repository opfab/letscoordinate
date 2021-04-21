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

package org.lfenergy.letscoordinate.scanner.service;

import io.vavr.control.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.common.monitoring.MonitoredTask;
import org.lfenergy.letscoordinate.scanner.config.LetscoProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class MonitoringServiceTest {

    LetscoProperties letscoProperties;
    @MockBean
    RestTemplate restTemplate;
    MonitoringService monitoringService;

    @BeforeEach
    public void before() {
        letscoProperties = LetscoProperties.builder()
                .backend(LetscoProperties.Backend.builder()
                        .baseUrl("http://base_url")
                        .build())
                .build();
        monitoringService = new MonitoringService(letscoProperties, restTemplate);
    }

    @Test
    public void saveMonitoredTask_success() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok(1L));
        Validation<String, Long> validation = monitoringService.saveMonitoredTask(new MonitoredTask());
        assertTrue(validation.isValid());
    }

    @Test
    public void saveMonitoredTask_fail() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenThrow(new RuntimeException("Error!"));
        Validation<String, Long> validation = monitoringService.saveMonitoredTask(new MonitoredTask());
        assertTrue(validation.isInvalid());
    }

}
