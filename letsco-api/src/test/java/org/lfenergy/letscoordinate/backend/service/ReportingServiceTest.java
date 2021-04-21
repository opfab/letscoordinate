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
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.config.LetscoProperties;
import org.lfenergy.letscoordinate.backend.dto.reporting.RscKpiReportDataDto;
import org.lfenergy.letscoordinate.backend.dto.reporting.RscKpiReportInitialFormDataDto;
import org.lfenergy.letscoordinate.backend.dto.reporting.RscKpiReportSubmittedFormDataDto;
import org.lfenergy.letscoordinate.backend.enums.DataGranularityEnum;
import org.lfenergy.letscoordinate.backend.enums.KpiDataTypeEnum;
import org.lfenergy.letscoordinate.backend.enums.ReportTypeEnum;
import org.lfenergy.letscoordinate.backend.model.RscKpiData;
import org.lfenergy.letscoordinate.backend.model.User;
import org.lfenergy.letscoordinate.backend.model.UserService;
import org.lfenergy.letscoordinate.backend.processor.ExcelDataProcessor;
import org.lfenergy.letscoordinate.backend.repository.RscKpiDataRepository;
import org.lfenergy.letscoordinate.backend.repository.UserRepository;
import org.lfenergy.letscoordinate.backend.util.ApplicationContextUtil;
import org.lfenergy.letscoordinate.backend.util.Constants;
import org.lfenergy.letscoordinate.backend.util.RscKpiFactory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReportingServiceTest {

    ReportingService reportingService;
    CoordinationConfig coordinationConfig;
    ExcelDataProcessor excelDataProcessor;
    EventMessageService eventMessageService;
    LetscoProperties letscoProperties;
    @Mock
    UserRepository userRepository;
    @Mock
    RscKpiDataRepository rscKpiDataRepository;

    @BeforeEach
    public void before() {
        letscoProperties = ApplicationContextUtil.initLetscoProperties();
        coordinationConfig = ApplicationContextUtil.initCoordinationConfig();
        eventMessageService = new EventMessageService(coordinationConfig, letscoProperties);
        excelDataProcessor = new ExcelDataProcessor(letscoProperties, coordinationConfig, eventMessageService);
        reportingService = new ReportingService(coordinationConfig, rscKpiDataRepository, excelDataProcessor, userRepository);

        SecurityContextHolder.getContext().setAuthentication(ApplicationContextUtil.createTestAuthentication());
    }

    @Test
    public void initRscKpiReportFormDto_shouldReturnRscKpiReportInitialFormDataDto() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(User.builder()
                .username("user.test")
                .userServices(Arrays.asList(
                        UserService.builder().serviceCode("SERVICE_A").build(),
                        UserService.builder().serviceCode("SERVICE_B").build()
                        ))
                .build()));
        Validation<String, RscKpiReportInitialFormDataDto> validation = reportingService.initRscKpiReportFormDto();
        assertAll(
                () -> assertTrue(validation.isValid()),
                () -> assertEquals(6, validation.get().getRscs().size()),
                () -> assertEquals(2, validation.get().getRegions().size()),
                () -> assertEquals(2, validation.get().getRscServices().size()),
                () -> assertEquals(2, validation.get().getKpiDataTypes().size())
        );
    }

    @Test
    public void initRscKpiReportFormDto_shouldReturnErrorMessage() {
        SecurityContextHolder.getContext().setAuthentication(null);
        Validation<String, RscKpiReportInitialFormDataDto> validation = reportingService.initRscKpiReportFormDto();
        assertAll(
                () -> assertTrue(validation.isInvalid()),
                () -> assertEquals("UNAUTHORIZED!", validation.getError())
        );
    }

    @Test
    public void getRscKpiReportDataForWebReport_shouldReturnRscKpiReportDataDto_rscsSelected() {
        List<RscKpiData> rscKpiDataList = Arrays.asList(RscKpiFactory.createRscKpiData());
        when(rscKpiDataRepository.findReportingKpis(anyString(), any(LocalDate.class), any(LocalDate.class),
                anyString(), anyString(), any(List.class))).thenReturn(rscKpiDataList);
        RscKpiReportSubmittedFormDataDto submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .rscServiceCode("SERVICE_A")
                .dataGranularity(DataGranularityEnum.DAILY)
                .startDate(LocalDate.of(2020, 1, 15))
                .endDate(LocalDate.of(2021, 1, 15))
                .rscCodes(Arrays.asList(Constants.ALL_RSCS_CODE))
                .kpiDataTypeCode("ALL")
                .build();
        RscKpiReportDataDto rscKpiReportDataDto = reportingService.getRscKpiReportDataForWebReport(submittedFormDataDto);
        assertAll(
                () -> assertNotNull(rscKpiReportDataDto),
                () -> assertSame(submittedFormDataDto, rscKpiReportDataDto.getSubmittedFormData()),
                () -> assertEquals(2, rscKpiReportDataDto.getRscKpiTypedDataMap().size()),
                () -> assertTrue(rscKpiReportDataDto.getRscKpiTypedDataMap().containsKey(KpiDataTypeEnum.GP)),
                () -> assertEquals(2, rscKpiReportDataDto.getRscKpiTypedDataMap().get(KpiDataTypeEnum.GP).size()),
                () -> assertTrue(rscKpiReportDataDto.getRscKpiTypedDataMap().containsKey(KpiDataTypeEnum.BP)),
                () -> assertEquals(3, rscKpiReportDataDto.getRscKpiTypedDataMap().get(KpiDataTypeEnum.BP).size()),
                () -> assertEquals(5, rscKpiReportDataDto.getRscKpiSubtypedDataMap().size()),
                () -> assertTrue(rscKpiReportDataDto.getRscKpiSubtypedDataMap().keySet().containsAll(Arrays.asList("GP1", "GP2", "BP1", "BP2", "BP3"))),
                () -> assertEquals("serviceA_allKpis_panEu_20200115_20210115", rscKpiReportDataDto.getReportFileName())
        );
    }

    @Test
    public void getRscKpiReportDataForWebReport_shouldReturnRscKpiReportDataDto_regionsSelected() {
        List<RscKpiData> rscKpiDataList = Arrays.asList(RscKpiFactory.createRscKpiData());
        when(rscKpiDataRepository.findReportingKpis(anyString(), any(LocalDate.class), any(LocalDate.class),
                anyString(), anyString(), any(List.class))).thenReturn(rscKpiDataList);
        RscKpiReportSubmittedFormDataDto submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .rscServiceCode("SERVICE_A")
                .dataGranularity(DataGranularityEnum.DAILY)
                .startDate(LocalDate.of(2020, 1, 15))
                .endDate(LocalDate.of(2021, 1, 15))
                .regionCodes(Arrays.asList(Constants.ALL_REGIONS_CODE))
                .kpiDataTypeCode("ALL")
                .build();
        RscKpiReportDataDto rscKpiReportDataDto = reportingService.getRscKpiReportDataForWebReport(submittedFormDataDto);
        assertAll(
                () -> assertNotNull(rscKpiReportDataDto),
                () -> assertSame(submittedFormDataDto, rscKpiReportDataDto.getSubmittedFormData()),
                () -> assertEquals(2, rscKpiReportDataDto.getRscKpiTypedDataMap().size()),
                () -> assertTrue(rscKpiReportDataDto.getRscKpiTypedDataMap().containsKey(KpiDataTypeEnum.GP)),
                () -> assertEquals(2, rscKpiReportDataDto.getRscKpiTypedDataMap().get(KpiDataTypeEnum.GP).size()),
                () -> assertTrue(rscKpiReportDataDto.getRscKpiTypedDataMap().containsKey(KpiDataTypeEnum.BP)),
                () -> assertEquals(3, rscKpiReportDataDto.getRscKpiTypedDataMap().get(KpiDataTypeEnum.BP).size()),
                () -> assertEquals(5, rscKpiReportDataDto.getRscKpiSubtypedDataMap().size()),
                () -> assertTrue(rscKpiReportDataDto.getRscKpiSubtypedDataMap().keySet().containsAll(Arrays.asList("GP1", "GP2", "BP1", "BP2", "BP3"))),
                () -> assertEquals("serviceA_allKpis_panEu_20200115_20210115", rscKpiReportDataDto.getReportFileName())
        );
    }

    @Test
    public void getRscKpiReportDataForExcelExportReport_dailyGranularityAndPanEuRscSelected() {
        List<RscKpiData> rscKpiDataList = Arrays.asList(RscKpiFactory.createRscKpiData());
        when(rscKpiDataRepository.findReportingKpis(anyString(), any(LocalDate.class), any(LocalDate.class),
                anyString(), anyString(), any(List.class))).thenReturn(rscKpiDataList);
        RscKpiReportSubmittedFormDataDto submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .rscServiceCode("SERVICE_A")
                .dataGranularity(DataGranularityEnum.DAILY)
                .startDate(LocalDate.of(2020, 1, 15))
                .endDate(LocalDate.of(2021, 1, 15))
                .rscCodes(Arrays.asList(Constants.ALL_RSCS_CODE))
                .kpiDataTypeCode("ALL")
                .build();
        RscKpiReportDataDto rscKpiReportDataDto = reportingService.getRscKpiReportDataForExcelExportReport(submittedFormDataDto);
        assertAll(
                () -> assertNotNull(rscKpiReportDataDto),
                () -> assertSame(submittedFormDataDto, rscKpiReportDataDto.getSubmittedFormData()),
                () -> assertEquals(2, rscKpiReportDataDto.getRscKpiTypedDataMap().size()),
                () -> assertTrue(rscKpiReportDataDto.getRscKpiTypedDataMap().containsKey(KpiDataTypeEnum.GP)),
                () -> assertEquals(2, rscKpiReportDataDto.getRscKpiTypedDataMap().get(KpiDataTypeEnum.GP).size()),
                () -> assertTrue(rscKpiReportDataDto.getRscKpiTypedDataMap().containsKey(KpiDataTypeEnum.BP)),
                () -> assertEquals(3, rscKpiReportDataDto.getRscKpiTypedDataMap().get(KpiDataTypeEnum.BP).size()),
                () -> assertEquals(5, rscKpiReportDataDto.getRscKpiSubtypedDataMap().size()),
                () -> assertTrue(rscKpiReportDataDto.getRscKpiSubtypedDataMap().keySet().containsAll(Arrays.asList("GP1", "GP2", "BP1", "BP2", "BP3"))),
                () -> assertEquals("serviceA_allKpis_panEu_20200115_20210115", rscKpiReportDataDto.getReportFileName())
        );
    }

    @Test
    public void getRscKpiReportDataForExcelExportReport_dailyGranularityAndPanEuRegionSelected() {
        List<RscKpiData> rscKpiDataList = Arrays.asList(RscKpiFactory.createRscKpiData());
        when(rscKpiDataRepository.findReportingKpis(anyString(), any(LocalDate.class), any(LocalDate.class),
                anyString(), anyString(), any(List.class))).thenReturn(rscKpiDataList);
        RscKpiReportSubmittedFormDataDto submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .rscServiceCode("SERVICE_A")
                .dataGranularity(DataGranularityEnum.DAILY)
                .startDate(LocalDate.of(2020, 1, 15))
                .endDate(LocalDate.of(2021, 1, 15))
                .regionCodes(Arrays.asList(Constants.ALL_REGIONS_CODE))
                .kpiDataTypeCode("ALL")
                .build();
        RscKpiReportDataDto rscKpiReportDataDto = reportingService.getRscKpiReportDataForExcelExportReport(submittedFormDataDto);
        assertAll(
                () -> assertNotNull(rscKpiReportDataDto),
                () -> assertSame(submittedFormDataDto, rscKpiReportDataDto.getSubmittedFormData()),
                () -> assertEquals(2, rscKpiReportDataDto.getRscKpiTypedDataMap().size()),
                () -> assertTrue(rscKpiReportDataDto.getRscKpiTypedDataMap().containsKey(KpiDataTypeEnum.GP)),
                () -> assertEquals(2, rscKpiReportDataDto.getRscKpiTypedDataMap().get(KpiDataTypeEnum.GP).size()),
                () -> assertTrue(rscKpiReportDataDto.getRscKpiTypedDataMap().containsKey(KpiDataTypeEnum.BP)),
                () -> assertEquals(3, rscKpiReportDataDto.getRscKpiTypedDataMap().get(KpiDataTypeEnum.BP).size()),
                () -> assertEquals(5, rscKpiReportDataDto.getRscKpiSubtypedDataMap().size()),
                () -> assertTrue(rscKpiReportDataDto.getRscKpiSubtypedDataMap().keySet().containsAll(Arrays.asList("GP1", "GP2", "BP1", "BP2", "BP3"))),
                () -> assertEquals("serviceA_allKpis_panEu_20200115_20210115", rscKpiReportDataDto.getReportFileName())
        );
    }

    @Test
    public void fillKpiDataSubtypeWithJoinGraphValues_shouldReturnNull() {
        assertNull(reportingService.fillKpiDataSubtypeWithJoinGraphValues(null, new ArrayList<>()));
        assertNull(reportingService.fillKpiDataSubtypeWithJoinGraphValues(new HashMap<>(), null));
    }

    @Test
    public void generateReportFileName_shouldGenerateFile() throws IOException {
        RscKpiReportSubmittedFormDataDto submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .rscServiceCode("SERVICE_A")
                .dataGranularity(DataGranularityEnum.DAILY)
                .startDate(LocalDate.of(2020, 1, 15))
                .endDate(LocalDate.of(2021, 1, 15))
                .regionCodes(Arrays.asList(Constants.ALL_REGIONS_CODE))
                .kpiDataTypeCode("ALL")
                .build();
        byte[] file = reportingService.generateRscKpiExcelReport(submittedFormDataDto);
        assertNotNull(file);
    }

    @Test
    public void generateReportFileName_knownService_allKpis_panEuRsc_daily_pdf() {
        RscKpiReportSubmittedFormDataDto submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .rscServiceCode("SERVICE_A")
                .dataGranularity(DataGranularityEnum.DAILY)
                .startDate(LocalDate.of(2020, 1, 15))
                .endDate(LocalDate.of(2021, 1, 15))
                .rscCodes(Arrays.asList(Constants.ALL_RSCS_CODE))
                .kpiDataTypeCode("ALL")
                .build();
        String filename = reportingService.generateReportFileName(submittedFormDataDto, ReportTypeEnum.PDF);
        assertEquals("serviceA_allKpis_panEu_20200115_20210115.pdf", filename);
    }

    @Test
    public void generateReportFileName_knownService_gpKpis_specificRsc_yearly_excel() {
        RscKpiReportSubmittedFormDataDto submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .rscServiceCode("SERVICE_A")
                .dataGranularity(DataGranularityEnum.YEARLY)
                .startDate(LocalDate.of(2020, 1, 15))
                .endDate(LocalDate.of(2021, 1, 15))
                .rscCodes(Arrays.asList("22XCORESO------S"))
                .kpiDataTypeCode("GP")
                .build();
        String filename = reportingService.generateReportFileName(submittedFormDataDto, ReportTypeEnum.EXCEL);
        assertEquals("serviceA_gpKpis_coresoRSC_2020_2021.xlsx", filename);
    }

    @Test
    public void generateReportFileNameWithoutExtension_shouldReturnNull() {
        assertNull(reportingService.generateReportFileNameWithoutExtension(null));
    }

    @Test
    public void getSelectedEntitiesForExportFileName_shouldReturnEmpty_nullInput() {
        assertEquals("", reportingService.getSelectedEntitiesForExportFileName(null));
    }

    @Test
    public void getSelectedEntitiesForExportFileName_shouldReturnEmpty_emptyRscOrRegionList() {
        RscKpiReportSubmittedFormDataDto submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .rscCodes(Arrays.asList())
                .regionCodes(Arrays.asList())
                .build();
        assertEquals("", reportingService.getSelectedEntitiesForExportFileName(submittedFormDataDto));
    }

    @Test
    public void getSelectedEntitiesForExportFileName_PanEuRsc() {
        RscKpiReportSubmittedFormDataDto submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .rscCodes(Arrays.asList(Constants.ALL_RSCS_CODE))
                .build();
        String filename = reportingService.getSelectedEntitiesForExportFileName(submittedFormDataDto);
        assertEquals("panEu", filename);
    }

    @Test
    public void getSelectedEntitiesForExportFileName_specificRsc() {
        RscKpiReportSubmittedFormDataDto submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .rscCodes(Arrays.asList("22XCORESO------S"))
                .build();
        String filename = reportingService.getSelectedEntitiesForExportFileName(submittedFormDataDto);
        assertEquals("coresoRSC", filename);
    }

    @Test
    public void getSelectedEntitiesForExportFileName_manyRscs() {
        RscKpiReportSubmittedFormDataDto submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .rscCodes(Arrays.asList(Constants.ALL_RSCS_CODE, "38X-BALTIC-RSC-H", "22XCORESO------S"))
                .build();
        String filename = reportingService.getSelectedEntitiesForExportFileName(submittedFormDataDto);
        assertEquals("3RSCs", filename);
    }

    @Test
    public void getSelectedEntitiesForExportFileName_PanEuRegion() {
        RscKpiReportSubmittedFormDataDto submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .regionCodes(Arrays.asList(Constants.ALL_REGIONS_CODE))
                .build();
        String filename = reportingService.getSelectedEntitiesForExportFileName(submittedFormDataDto);
        assertEquals("panEu", filename);
    }

    @Test
    public void getSelectedEntitiesForExportFileName_specificRegion() {
        RscKpiReportSubmittedFormDataDto submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .regionCodes(Arrays.asList("10Y1001C--00095L"))
                .build();
        String filename = reportingService.getSelectedEntitiesForExportFileName(submittedFormDataDto);
        assertEquals("sweRegion", filename);
    }

    @Test
    public void getSelectedEntitiesForExportFileName_manyRegions() {
        RscKpiReportSubmittedFormDataDto submittedFormDataDto = RscKpiReportSubmittedFormDataDto.builder()
                .regionCodes(Arrays.asList(Constants.ALL_REGIONS_CODE, "10Y1001C--00095L"))
                .build();
        String filename = reportingService.getSelectedEntitiesForExportFileName(submittedFormDataDto);
        assertEquals("2Regions", filename);
    }

}
