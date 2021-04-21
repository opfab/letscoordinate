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
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.dto.reporting.*;
import org.lfenergy.letscoordinate.backend.enums.KpiDataSubtypeEnum;
import org.lfenergy.letscoordinate.backend.enums.KpiDataTypeEnum;
import org.lfenergy.letscoordinate.backend.model.RscKpi;
import org.lfenergy.letscoordinate.backend.model.RscKpiData;
import org.lfenergy.letscoordinate.backend.model.RscKpiDataDetails;
import org.lfenergy.letscoordinate.backend.util.Constants;

import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RscKpiReportMapper {

    public static RscDto toDto(CoordinationConfig.Rsc entity) {
        return Optional.ofNullable(entity)
                .map(rsc -> RscDto.builder()
                        .eicCode(rsc.getEicCode())
                        .name(rsc.getName())
                        .shortName(rsc.getShortName())
                        .index(rsc.getIndex())
                        .build())
                .orElse(null);
    }

    public static RegionDto toDto(CoordinationConfig.Region entity) {
        return Optional.ofNullable(entity)
                .map(region -> RegionDto.builder()
                        .eicCode(region.getEicCode())
                        .name(region.getName())
                        .shortName(region.getShortName())
                        .index(region.getIndex())
                        .build())
                .orElse(null);
    }

    public static UserServiceDto toDto(CoordinationConfig.Service entity) {
        return Optional.ofNullable(entity)
                .map(rscService -> UserServiceDto.builder()
                        .code(rscService.getCode())
                        .name(rscService.getName())
                        .build())
                .orElse(null);
    }

    public static KpiDataTypeDto toDto(CoordinationConfig.KpiDataType entity) {
        return Optional.ofNullable(entity)
                .map(kpiDataType -> KpiDataTypeDto.builder()
                        .code(kpiDataType.getCode())
                        .name(kpiDataType.getName())
                        .index(kpiDataType.getIndex())
                        .build())
                .orElse(null);
    }

    public static Map<KpiDataTypeEnum, Map<KpiDataSubtypeEnum, Map<String, List<RscKpiDto.DataDto>>>> toMap(List<RscKpi> rscKpis,
                                                                                                            Map<KpiDataTypeEnum, List<KpiDataSubtypeEnum>> kpiDataSubtypeEnumMap,
                                                                                                            String selectedKpiDataTypeCode) {
        if(rscKpis == null || kpiDataSubtypeEnumMap == null)
            return null;
        // If selectedKpiDataTypeCode != ALL , then keep only the selected one from the kpiDataSubtypeEnumMap
        if (!Constants.ALL_DATA_TYPE_CODE.equals(selectedKpiDataTypeCode)) {
            KpiDataTypeEnum selectedKpiDataTypeEnum = KpiDataTypeEnum.getByName(selectedKpiDataTypeCode);
            List<KpiDataSubtypeEnum> KpiDataSubtypeEnumList = kpiDataSubtypeEnumMap.get(selectedKpiDataTypeEnum);
            kpiDataSubtypeEnumMap = new HashMap<>();
            kpiDataSubtypeEnumMap.put(selectedKpiDataTypeEnum, KpiDataSubtypeEnumList);
        }
        // Group rscKpis in map having KpiDataTypeEnum (GP or BP) as key
        Map<KpiDataTypeEnum, Map<KpiDataSubtypeEnum, List<RscKpiDto>>> mapTmp = rscKpis.stream()
                .map(RscKpiReportMapper::toDto)
                .collect(Collectors.groupingBy(RscKpiDto::getKpiDataType, Collectors.groupingBy(RscKpiDto::getKpiDataSubtype)));
        Map<KpiDataTypeEnum, Map<KpiDataSubtypeEnum, Map<String, List<RscKpiDto.DataDto>>>> result = new LinkedHashMap<>();
        for(KpiDataTypeEnum kpiDataTypeEnum : kpiDataSubtypeEnumMap.keySet()) {
            if(kpiDataTypeEnum != KpiDataTypeEnum.UNKNOWN) {
                result.put(kpiDataTypeEnum, new LinkedHashMap<>());
                Map<KpiDataSubtypeEnum, List<RscKpiDto>> rscKpiTmpMap = mapTmp.get(kpiDataTypeEnum);
                if (rscKpiTmpMap == null) {
                    mapTmp.put(kpiDataTypeEnum, new LinkedHashMap<>());
                    rscKpiTmpMap = mapTmp.get(kpiDataTypeEnum);
                }
                for (KpiDataSubtypeEnum kpiDataSubtypeEnum : kpiDataSubtypeEnumMap.get(kpiDataTypeEnum)) {
                    List<RscKpiDto> rscKpiTmpList = rscKpiTmpMap.get(kpiDataSubtypeEnum);
                    if (rscKpiTmpList != null) {
                        Map<String, List<RscKpiDto.DataDto>> rscKpiDataMap = rscKpiTmpList.stream()
                                .map(RscKpiDto::getDataMap)
                                .map(Map::values)
                                .flatMap(Collection::stream)
                                .flatMap(Collection::stream)
                                .sorted(Comparator.comparing(RscKpiDto.DataDto::getTimestamp))
                                .collect(Collectors.groupingBy(RscKpiDto.DataDto::getLabel));
                        result.get(kpiDataTypeEnum).put(kpiDataSubtypeEnum, rscKpiDataMap);
                    } else {
                        result.get(kpiDataTypeEnum).put(kpiDataSubtypeEnum, new LinkedHashMap<>());
                    }
                }
            }
        }

        return result;
    }

    public static RscKpiDto toDto(RscKpi rscKpi) {
        if (rscKpi == null)
            return null;
        return RscKpiDto.builder()
                .name(rscKpi.getName())
                .joinGraph(rscKpi.getJoinGraph())
                .dataMap(rscKpi.getRscKpiDatas().stream()
                        .sorted(Comparator.comparing(RscKpiData::getTimestamp))
                        .map(RscKpiReportMapper::toDto)
                        .collect(Collectors.groupingBy(RscKpiDto.DataDto::getLabel)))
                .build();
    }

    private static RscKpiDto.DataDto toDto(RscKpiData rscKpiData) {
        return Optional.ofNullable(rscKpiData)
                .map(data -> RscKpiDto.DataDto.builder()
                        .timestamp(rscKpiData.getTimestamp().toLocalDate())
                        .dataGranularity(rscKpiData.getGranularity())
                        .label(rscKpiData.getLabel())
                        .details(rscKpiData.getRscKpiDataDetails().stream()
                                .map(RscKpiReportMapper::toDto)
                                .collect(Collectors.toList()))
                        .build())
                .orElse(null);
    }

    private static RscKpiDto.DataDto.DetailsDto toDto(RscKpiDataDetails rscKpiDataDetails) {
        return Optional.ofNullable(rscKpiDataDetails)
                .map(details -> RscKpiDto.DataDto.DetailsDto.builder()
                        .value(details.getValue())
                        .eicCode(details.getEicCode())
                        .build())
                .orElse(null);
    }

}
