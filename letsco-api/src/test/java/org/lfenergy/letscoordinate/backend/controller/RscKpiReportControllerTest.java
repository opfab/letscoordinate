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
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.dto.reporting.RscKpiReportSubmittedFormDataDto;
import org.lfenergy.letscoordinate.backend.enums.DataGranularityEnum;
import org.lfenergy.letscoordinate.backend.model.User;
import org.lfenergy.letscoordinate.backend.model.UserService;
import org.lfenergy.letscoordinate.backend.processor.ExcelDataProcessor;
import org.lfenergy.letscoordinate.backend.repository.RscKpiDataRepository;
import org.lfenergy.letscoordinate.backend.repository.UserRepository;
import org.lfenergy.letscoordinate.backend.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RscKpiReportControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    CoordinationConfig coordinationConfig;
    @Autowired
    ExcelDataProcessor excelDataProcessor;
    @MockBean
    UserRepository userRepository;
    @MockBean
    RscKpiDataRepository rscKpiDataRepository;

    @Test
    @WithMockCustomUser
    public void getUserByUsername_shouldReturn200() throws Exception {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(User.builder()
                .id(1L)
                .username("user.test")
                .userServices(Arrays.asList(
                        UserService.builder().serviceCode("SERVICE_A").build(),
                        UserService.builder().serviceCode("SERVICE_B").build()
                ))
                .build()));
        mockMvc.perform(get("/letsco/api/v1/rsc-kpi-report/config-data"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rscs", hasSize(6)))
                .andExpect(jsonPath("$.regions", hasSize(10)))
                .andExpect(jsonPath("$.rscServices", hasSize(2)))
                .andExpect(jsonPath("$.kpiDataTypes", hasSize(2)));
    }

    @Test
    @WithMockUser(value = "user.test", authorities = {"ROLE_TSO"})
    public void getUserByUsername_shouldReturn401_invalidToken() throws Exception {
        mockMvc.perform(get("/letsco/api/v1/rsc-kpi-report/config-data"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").value("UNAUTHORIZED!"));
    }

    @Test
    public void getUserByUsername_shouldReturn401_unknownToken() throws Exception {
        mockMvc.perform(get("/letsco/api/v1/rsc-kpi-report/config-data"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser
    public void getKpis_shouldReturn200() throws Exception {
        RscKpiReportSubmittedFormDataDto submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .rscServiceCode("SERVICE_A")
                .dataGranularity(DataGranularityEnum.DAILY)
                .startDate(LocalDate.of(2020, 1, 15))
                .endDate(LocalDate.of(2021, 1, 15))
                .rscCodes(Arrays.asList(Constants.ALL_RSCS_CODE))
                .kpiDataTypeCode("ALL")
                .build();
        mockMvc.perform(post("/letsco/api/v1/rsc-kpi-report/kpis")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(submittedFormDataDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submittedFormData.dataGranularity").value("D"))
                .andExpect(jsonPath("$.submittedFormData.startDate").value("2020-01-15"))
                .andExpect(jsonPath("$.submittedFormData.endDate").value("2021-01-15"))
                .andExpect(jsonPath("$.submittedFormData.rscCodes", hasSize(1)))
                .andExpect(jsonPath("$.submittedFormData.rscServiceCode").value("SERVICE_A"))
                .andExpect(jsonPath("$.submittedFormData.kpiDataTypeCode").value("ALL"))
                .andExpect(jsonPath("$.rscKpiTypedDataMap", hasKey("GP")))
                .andExpect(jsonPath("$.rscKpiTypedDataMap", hasKey("BP")))
                .andExpect(jsonPath("$.rscKpiSubtypedDataMap", hasKey("GP01")))
                .andExpect(jsonPath("$.rscKpiSubtypedDataMap", hasKey("GP02")))
                .andExpect(jsonPath("$.rscKpiSubtypedDataMap", hasKey("BP01")))
                .andExpect(jsonPath("$.rscKpiSubtypedDataMap", hasKey("BP02")))
                .andExpect(jsonPath("$.rscKpiSubtypedDataMap", hasKey("BP03")))
                .andExpect(jsonPath("$.rscKpiSubtypedDataMap", hasKey("BP04")))
                .andExpect(jsonPath("$.rscKpiSubtypedDataMap", hasKey("BP05")))
                .andExpect(jsonPath("$.rscKpiSubtypedDataMap", hasKey("BP06")))
                .andExpect(jsonPath("$.rscKpiSubtypedDataMap", hasKey("BP07")))
                .andExpect(jsonPath("$.reportFileName").value("serviceA_allKpis_panEu_20200115_20210115"))
        ;
    }

    @Test
    @WithMockCustomUser
    public void downloadRscKpiExcelReport_shouldReturn200() throws Exception {
        RscKpiReportSubmittedFormDataDto submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .rscServiceCode("SERVICE_A")
                .dataGranularity(DataGranularityEnum.DAILY)
                .startDate(LocalDate.of(2020, 1, 15))
                .endDate(LocalDate.of(2021, 1, 15))
                .rscCodes(Arrays.asList(Constants.ALL_RSCS_CODE))
                .kpiDataTypeCode("ALL")
                .build();
        mockMvc.perform(post("/letsco/api/v1/rsc-kpi-report/download/excel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(submittedFormDataDto)))
                .andExpect(status().isOk());
    }

}
