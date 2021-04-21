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

package org.lfenergy.letscoordinate.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.backend.dto.monitoring.MonitoredTaskDto;
import org.lfenergy.letscoordinate.backend.dto.monitoring.MonitoredTaskStepDto;
import org.lfenergy.letscoordinate.backend.model.MonitoredTask;
import org.lfenergy.letscoordinate.backend.repository.MonitoredTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MonitoringControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    MonitoredTaskRepository monitoredTaskRepository;

    @Test
    @WithMockCustomUser
    public void saveMonitoredTask_shouldReturn200() throws Exception {
        MonitoredTaskDto monitoredTaskDto = MonitoredTaskDto.builder()
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
        mockMvc.perform(post("/letsco/api/v1/monitoring")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(monitoredTaskDto)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void saveMonitoredTask_shouldReturn500() throws Exception {
        MonitoredTaskDto monitoredTaskDto = MonitoredTaskDto.builder()
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
        when(monitoredTaskRepository.save(any(MonitoredTask.class))).thenThrow(RuntimeException.class);
        mockMvc.perform(post("/letsco/api/v1/monitoring")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(monitoredTaskDto)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(jsonPath("$.code").value("ERROR"))
                .andExpect(jsonPath("$.messages", hasSize(1)));
    }

}
