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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.dto.reporting.*;
import org.lfenergy.letscoordinate.backend.enums.DataGranularityEnum;
import org.lfenergy.letscoordinate.backend.enums.KpiDataSubtypeEnum;
import org.lfenergy.letscoordinate.backend.enums.KpiDataTypeEnum;
import org.lfenergy.letscoordinate.backend.model.RscKpi;
import org.lfenergy.letscoordinate.backend.model.RscKpiData;
import org.lfenergy.letscoordinate.backend.model.RscKpiDataDetails;
import org.lfenergy.letscoordinate.backend.util.ApplicationContextUtil;
import org.lfenergy.letscoordinate.backend.util.Constants;
import org.lfenergy.letscoordinate.backend.util.RscKpiFactory;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class RscKpiReportMapperTest {

    @Test
    public void toDto_shouldReturnNullWhenInputRscEntityIsNull() {
        CoordinationConfig.Rsc entity = null;
        RscDto dto = RscKpiReportMapper.toDto(entity);
        assertNull(dto);
    }

    @Test
    public void toDto_shouldReturnDtoWhenInputRscEntityIsNotNull() {
        CoordinationConfig.Rsc entity = CoordinationConfig.Rsc.builder()
                .name("Baltic RSC")
                .shortName("Baltic RSC")
                .eicCode("38X-BALTIC-RSC-H")
                .index(1)
                .build();
        RscDto dto = RscKpiReportMapper.toDto(entity);
        assertAll(
                () -> assertNotNull(dto),
                () -> assertEquals("Baltic RSC", dto.getName()),
                () -> assertEquals("Baltic RSC", dto.getShortName()),
                () -> assertEquals("38X-BALTIC-RSC-H", dto.getEicCode()),
                () -> assertEquals(1, dto.getIndex())
        );
    }

    @Test
    public void toDto_shouldReturnNullWhenInputRegionEntityIsNull() {
        CoordinationConfig.Region entity = null;
        RegionDto dto = RscKpiReportMapper.toDto(entity);
        assertNull(dto);
    }

    @Test
    public void toDto_shouldReturnDtoWhenInputRegionEntityIsNotNull() {
        CoordinationConfig.Region entity = CoordinationConfig.Region.builder()
                .name("Baltic")
                .shortName("Baltic")
                .eicCode("EICCODE-REGION-0")
                .index(1)
                .build();
        RegionDto dto = RscKpiReportMapper.toDto(entity);
        assertAll(
                () -> assertNotNull(dto),
                () -> assertEquals("Baltic", dto.getName()),
                () -> assertEquals("Baltic", dto.getShortName()),
                () -> assertEquals("EICCODE-REGION-0", dto.getEicCode()),
                () -> assertEquals(1, dto.getIndex())
        );
    }

    @Test
    public void toDto_shouldReturnNullWhenInputServiceEntityIsNull() {
        CoordinationConfig.Service entity = null;
        UserServiceDto dto = RscKpiReportMapper.toDto(entity);
        assertNull(dto);
    }

    @Test
    public void toDto_shouldReturnDtoWhenInputServiceEntityIsNotNull() {
        CoordinationConfig.Service entity = CoordinationConfig.Service.builder()
                .code("SERVICE_A")
                .name("Service A")
                .build();
        UserServiceDto dto = RscKpiReportMapper.toDto(entity);
        assertAll(
                () -> assertNotNull(dto),
                () -> assertEquals("SERVICE_A", dto.getCode()),
                () -> assertEquals("Service A", dto.getName())
        );
    }

    @Test
    public void toDto_shouldReturnNullWhenInputKpiDataTypeEntityIsNull() {
        CoordinationConfig.KpiDataType entity = null;
        KpiDataTypeDto dto = RscKpiReportMapper.toDto(entity);
        assertNull(dto);
    }

    @Test
    public void toDto_shouldReturnDtoWhenInputKpiDataTypeEntityIsNotNull() {
        CoordinationConfig.KpiDataType entity = CoordinationConfig.KpiDataType.builder()
                .code("GP")
                .name("Global performance KPIs")
                .index(1)
                .build();
        KpiDataTypeDto dto = RscKpiReportMapper.toDto(entity);
        assertAll(
                () -> assertNotNull(dto),
                () -> assertEquals("GP", dto.getCode()),
                () -> assertEquals("Global performance KPIs", dto.getName()),
                () -> assertEquals(1, dto.getIndex())
        );
    }

    @Test
    public void toMap_shouldReturnNullWhenInputParamsNull() {
        List<RscKpi> rscKpis = new ArrayList<>();
        Map<KpiDataTypeEnum, List<KpiDataSubtypeEnum>> kpiDataSubtypeEnumMap = null;
        Map<KpiDataTypeEnum, Map<KpiDataSubtypeEnum, Map<String, List<RscKpiDto.DataDto>>>> map = RscKpiReportMapper.toMap(rscKpis, kpiDataSubtypeEnumMap, null);
        assertNull(map);

        rscKpis = null;
        kpiDataSubtypeEnumMap = new HashMap<>();
        map = RscKpiReportMapper.toMap(rscKpis, kpiDataSubtypeEnumMap, null);
        assertNull(map);
    }

    @Test
    public void toMap_shouldReturnMap_allDataType() {
        List<RscKpi> rscKpis = Arrays.asList(RscKpiFactory.createRscKpi());
        Map<KpiDataTypeEnum, List<KpiDataSubtypeEnum>> kpiDataSubtypeEnumMap = ApplicationContextUtil.initCoordinationConfig().getKpiDataTypeMapByServiceCode("SERVICE_A");
        String selectedKpiDataTypeCode = Constants.ALL_DATA_TYPE_CODE;
        Map<KpiDataTypeEnum, Map<KpiDataSubtypeEnum, Map<String, List<RscKpiDto.DataDto>>>> map = RscKpiReportMapper.toMap(
                rscKpis,
                kpiDataSubtypeEnumMap,
                selectedKpiDataTypeCode
        );
        assertAll(
                () -> assertNotNull(map),
                () -> assertEquals(2, map.size()),
                () -> assertNotNull(map.get(KpiDataTypeEnum.GP)),
                () -> assertEquals(2, map.get(KpiDataTypeEnum.GP).size()),
                () -> assertNotNull(map.get(KpiDataTypeEnum.BP)),
                () -> assertEquals(3, map.get(KpiDataTypeEnum.BP).size())
        );

    }

    @Test
    public void toMap_shouldReturnMap_gpDataType() {
        List<RscKpi> rscKpis = Arrays.asList(RscKpiFactory.createRscKpi());
        Map<KpiDataTypeEnum, List<KpiDataSubtypeEnum>> kpiDataSubtypeEnumMap = ApplicationContextUtil.initCoordinationConfig().getKpiDataTypeMapByServiceCode("SERVICE_A");
        String selectedKpiDataTypeCode = "GP";
        Map<KpiDataTypeEnum, Map<KpiDataSubtypeEnum, Map<String, List<RscKpiDto.DataDto>>>> map = RscKpiReportMapper.toMap(
                rscKpis,
                kpiDataSubtypeEnumMap,
                selectedKpiDataTypeCode
        );
        assertAll(
                () -> assertNotNull(map),
                () -> assertEquals(1, map.size()),
                () -> assertNotNull(map.get(KpiDataTypeEnum.GP)),
                () -> assertEquals(2, map.get(KpiDataTypeEnum.GP).size()),
                () -> assertNull(map.get(KpiDataTypeEnum.BP))
        );

    }

    @Test
    public void toMap_shouldReturnMap_bpDataType() {
        List<RscKpi> rscKpis = Arrays.asList(RscKpiFactory.createRscKpi());
        Map<KpiDataTypeEnum, List<KpiDataSubtypeEnum>> kpiDataSubtypeEnumMap = ApplicationContextUtil.initCoordinationConfig().getKpiDataTypeMapByServiceCode("SERVICE_A");
        String selectedKpiDataTypeCode = "BP";
        Map<KpiDataTypeEnum, Map<KpiDataSubtypeEnum, Map<String, List<RscKpiDto.DataDto>>>> map = RscKpiReportMapper.toMap(
                rscKpis,
                kpiDataSubtypeEnumMap,
                selectedKpiDataTypeCode
        );
        assertAll(
                () -> assertNotNull(map),
                () -> assertEquals(1, map.size()),
                () -> assertNotNull(map.get(KpiDataTypeEnum.BP)),
                () -> assertEquals(3, map.get(KpiDataTypeEnum.BP).size()),
                () -> assertNull(map.get(KpiDataTypeEnum.GP))
        );

    }

    @Test
    public void toMap_shouldReturnMap_unknownDataType() {
        List<RscKpi> rscKpis = Arrays.asList(RscKpiFactory.createRscKpi());
        Map<KpiDataTypeEnum, List<KpiDataSubtypeEnum>> kpiDataSubtypeEnumMap = ApplicationContextUtil.initCoordinationConfig().getKpiDataTypeMapByServiceCode("SERVICE_A");
        String selectedKpiDataTypeCode = "UNKNOWN";
        Map<KpiDataTypeEnum, Map<KpiDataSubtypeEnum, Map<String, List<RscKpiDto.DataDto>>>> map = RscKpiReportMapper.toMap(
                rscKpis,
                kpiDataSubtypeEnumMap,
                selectedKpiDataTypeCode
        );
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    public void toDto_shouldReturnNullWhenInputRscKpiEntityIsNull() {
        RscKpi entity = null;
        RscKpiDto dto = RscKpiReportMapper.toDto(entity);
        assertNull(dto);
    }

    @Test
    public void toDto_shouldReturnDtoWhenInputRscKpiEntityIsNotNull() {
        RscKpi entity = RscKpiFactory.createRscKpi();
        RscKpiDto dto = RscKpiReportMapper.toDto(entity);
        assertAll(
                () -> assertNotNull(dto),
                () -> assertEquals("GP1", dto.getName()),
                () -> assertEquals(false, dto.getJoinGraph()),
                () -> assertEquals(false, dto.getDataMap().isEmpty()),
                () -> assertEquals(1, dto.getDataMap().size()),
                () -> assertNotNull(dto.getDataMap().get("Global Perf 1")),
                () -> assertNotNull(dto.getDataMap().get("Global Perf 1").get(0)),
                () -> assertEquals(DataGranularityEnum.DAILY, dto.getDataMap().get("Global Perf 1").get(0).getDataGranularity()),
                () -> assertEquals("Global Perf 1", dto.getDataMap().get("Global Perf 1").get(0).getLabel()),
                () -> assertEquals(LocalDate.of(2021, 2, 8), dto.getDataMap().get("Global Perf 1").get(0).getTimestamp()),
                () -> assertNotNull(dto.getDataMap().get("Global Perf 1").get(0).getDetails()),
                () -> assertEquals(1 , dto.getDataMap().get("Global Perf 1").get(0).getDetails().size()),
                () -> assertNotNull(dto.getDataMap().get("Global Perf 1").get(0).getDetails().get(0)),
                () -> assertEquals("10XFR-RTE------Q", dto.getDataMap().get("Global Perf 1").get(0).getDetails().get(0).getEicCode()),
                () -> assertEquals(1, dto.getDataMap().get("Global Perf 1").get(0).getDetails().get(0).getValue())
        );
    }

}
