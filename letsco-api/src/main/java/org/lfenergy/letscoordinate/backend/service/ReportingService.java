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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.dto.reporting.*;
import org.lfenergy.letscoordinate.backend.enums.DataGranularityEnum;
import org.lfenergy.letscoordinate.backend.enums.KpiDataSubtypeEnum;
import org.lfenergy.letscoordinate.backend.enums.ReportTypeEnum;
import org.lfenergy.letscoordinate.backend.mapper.RscKpiReportMapper;
import org.lfenergy.letscoordinate.backend.model.RscKpi;
import org.lfenergy.letscoordinate.backend.model.RscKpiData;
import org.lfenergy.letscoordinate.backend.model.User;
import org.lfenergy.letscoordinate.backend.model.UserService;
import org.lfenergy.letscoordinate.backend.processor.ExcelDataProcessor;
import org.lfenergy.letscoordinate.backend.repository.RscKpiDataRepository;
import org.lfenergy.letscoordinate.backend.repository.UserRepository;
import org.lfenergy.letscoordinate.backend.util.Constants;
import org.lfenergy.letscoordinate.backend.util.SecurityUtil;
import org.lfenergy.letscoordinate.backend.util.StringUtil;
import org.lfenergy.letscoordinate.common.exception.AuthorizationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportingService {

    private final CoordinationConfig coordinationConfig;
    private final RscKpiDataRepository rscKpiDataRepository;
    private final ExcelDataProcessor excelDataProcessor;
    private final UserRepository userRepository;

    public Validation<String, RscKpiReportInitialFormDataDto> initRscKpiReportFormDto() {
        final String username;
        try {
            username = SecurityUtil.getUsernameFromToken();
        } catch (AuthorizationException authEx) {
            return Validation.invalid(authEx.getMessage());
        }
        Optional<User> user = userRepository.findByUsername(username);

        return user.map(u -> Validation.<String, RscKpiReportInitialFormDataDto>valid(RscKpiReportInitialFormDataDto.builder()
                .rscs(coordinationConfig.getRscs().values().stream()
                        .map(RscKpiReportMapper::toDto)
                        .sorted(Comparator.comparing(RscDto::getIndex))
                        .collect(Collectors.toList()))
                .regions(coordinationConfig.getRegions().values().stream()
                        .map(RscKpiReportMapper::toDto)
                        .sorted(Comparator.comparing(RegionDto::getIndex))
                        .collect(Collectors.toList()))
                .rscServices(getCurrentUserRcsServices(u.getUserServices()))
                .kpiDataTypes(coordinationConfig.getKpiDataTypes().values().stream()
                        .map(RscKpiReportMapper::toDto)
                        .sorted(Comparator.comparing(KpiDataTypeDto::getIndex))
                        .collect(Collectors.toList()))
                .build()))
                .orElseGet(() -> Validation.invalid("User \"" + username + "\" not found!"));
    }

    @Transactional(readOnly = true)
    public RscKpiReportDataDto getRscKpiReportDataForWebReport(RscKpiReportSubmittedFormDataDto submittedFormDataDto) {
        List<RscKpi> rscKpiList = getRscKpiListForWebReport(submittedFormDataDto);
        return RscKpiReportDataDto.builder()
                .submittedFormData(submittedFormDataDto)
                .rscKpiTypedDataMap(RscKpiReportMapper.toMap(rscKpiList, coordinationConfig.getKpiDataTypeMapByServiceCode(submittedFormDataDto.getRscServiceCode()), submittedFormDataDto.getKpiDataTypeCode()))
                .rscKpiSubtypedDataMap(fillKpiDataSubtypeWithJoinGraphValues(coordinationConfig.getKpiDataSubtypesByServiceCode(submittedFormDataDto.getRscServiceCode()), rscKpiList))
                .reportFileName(generateReportFileNameWithoutExtension(submittedFormDataDto))
                .build();
    }

    @Transactional(readOnly = true)
    public RscKpiReportDataDto getRscKpiReportDataForExcelExportReport(RscKpiReportSubmittedFormDataDto submittedFormDataDto) {
        List<RscKpi> rscKpiList = getRscKpiListForExcelExportReport(submittedFormDataDto);
        return RscKpiReportDataDto.builder()
                .submittedFormData(submittedFormDataDto)
                .rscKpiTypedDataMap(RscKpiReportMapper.toMap(rscKpiList, coordinationConfig.getKpiDataTypeMapByServiceCode(submittedFormDataDto.getRscServiceCode()), submittedFormDataDto.getKpiDataTypeCode()))
                .rscKpiSubtypedDataMap(fillKpiDataSubtypeWithJoinGraphValues(coordinationConfig.getKpiDataSubtypesByServiceCode(submittedFormDataDto.getRscServiceCode()), rscKpiList))
                .reportFileName(generateReportFileNameWithoutExtension(submittedFormDataDto))
                .build();
    }

    protected Map<String, CoordinationConfig.KpiDataSubtype> fillKpiDataSubtypeWithJoinGraphValues(Map<String, CoordinationConfig.KpiDataSubtype> kpiDataSubtypeMap,
                                                                                                   List<RscKpi> rscKpiList) {
        if (rscKpiList == null || kpiDataSubtypeMap == null)
            return null;
        List<RscKpi> descSortedRscKpiList = rscKpiList.stream()
                .sorted(Comparator.comparing(RscKpi::getId).reversed())
                .collect(Collectors.toList());
        for (CoordinationConfig.KpiDataSubtype kpiDataSubtype : kpiDataSubtypeMap.values()) {
            for (RscKpi rscKpi : descSortedRscKpiList) {
                if (kpiDataSubtype.getCode() != null && kpiDataSubtype.getCode().equalsIgnoreCase(rscKpi.getName())) {
                    kpiDataSubtype.setJoinGraph(rscKpi.getJoinGraph());
                    break;
                }
            }
        }
        return kpiDataSubtypeMap;
    }

    /**
     * This function allow to get KPI data for the Web report
     *   - GP KPIs for selected RSCs or Regions
     *   - BP KPIs for selected RSCs or Regions
     *
     * @param submittedFormDataDto
     * @return
     */
    private List<RscKpi> getRscKpiListForWebReport(RscKpiReportSubmittedFormDataDto submittedFormDataDto) {
        return getRscKpiList(
                submittedFormDataDto.getDataGranularity(),
                submittedFormDataDto.getStartDate(),
                submittedFormDataDto.getEndDate(),
                submittedFormDataDto.getKpiDataTypeCode(),
                submittedFormDataDto.getRscServiceCode(),
                CollectionUtils.isNotEmpty(submittedFormDataDto.getRscCodes()) ? submittedFormDataDto.getRscCodes() : submittedFormDataDto.getRegionCodes()
        );
    }

    /**
     * This function allow to get KPI data for the Excel exported report
     *   - GP KPIs for all RSCs or Regions
     *   - BP KPIs for all RSCs or Regions
     *
     * @param submittedFormDataDto
     * @return
     */
    private List<RscKpi> getRscKpiListForExcelExportReport(RscKpiReportSubmittedFormDataDto submittedFormDataDto) {
        List<String> letscoEntityCodeList = new ArrayList<>();
        if(submittedFormDataDto == null)
            return new ArrayList<>();

        if (CollectionUtils.isNotEmpty(submittedFormDataDto.getRscCodes())) {
            if(submittedFormDataDto.getRscCodes().contains(Constants.ALL_RSCS_CODE))
                letscoEntityCodeList.add(Constants.ALL_RSCS_CODE);
            letscoEntityCodeList.addAll(coordinationConfig.getRscEicCodes());
        } else if (CollectionUtils.isNotEmpty(submittedFormDataDto.getRegionCodes())) {
            if(submittedFormDataDto.getRegionCodes().contains(Constants.ALL_REGIONS_CODE))
                letscoEntityCodeList.add(Constants.ALL_REGIONS_CODE);
            letscoEntityCodeList.addAll(coordinationConfig.getRegionEicCodes());
        }

        return getRscKpiList(
                submittedFormDataDto.getDataGranularity(),
                submittedFormDataDto.getStartDate(),
                submittedFormDataDto.getEndDate(),
                submittedFormDataDto.getKpiDataTypeCode(),
                submittedFormDataDto.getRscServiceCode(),
                letscoEntityCodeList
        );
    }

    private List<RscKpi> getRscKpiList(DataGranularityEnum dataGranularity,
                                       LocalDate startDate,
                                       LocalDate endDate,
                                       String kpiDataTypeCode,
                                       String rscServiceCode,
                                       List<String> letscoEntityCodes) {
        List<RscKpiData> rscKpiDataList = rscKpiDataRepository.findReportingKpis(
                dataGranularity != null ? dataGranularity.name() : "",
                startDate,
                endDate,
                Constants.ALL_DATA_TYPE_CODE.equalsIgnoreCase(kpiDataTypeCode) ? "" : kpiDataTypeCode,
                rscServiceCode,
                letscoEntityCodes);
        // filter by eicCode
        rscKpiDataList = rscKpiDataList.stream()
                .map(rscKpiData -> {
                    rscKpiData.setRscKpiDataDetails(rscKpiData.getRscKpiDataDetails().stream()
                            .filter(d -> StringUtils.isBlank(d.getEicCode()) || (CollectionUtils.isNotEmpty(letscoEntityCodes) && letscoEntityCodes.contains(d.getEicCode())))
                            .collect(Collectors.toList()));
                    return rscKpiData;
                })
                .collect(Collectors.toList());
        // group by RscKpi
        Map<RscKpi, List<RscKpiData>> mapTmp = rscKpiDataList.stream().collect(Collectors.groupingBy(RscKpiData::getRscKpi));
        return mapTmp.entrySet().stream()
                .map(entry -> {
                    entry.getKey().setRscKpiDatas(entry.getValue());
                    return entry.getKey();
                }).collect(Collectors.toList());
    }

    private List<UserServiceDto> getCurrentUserRcsServices(List<UserService> services) {
        if (CollectionUtils.isEmpty(services))
            return new ArrayList<>();
        List<String> userServicesCodes = services.stream()
                .map(UserService::getServiceCode)
                .collect(Collectors.toList());
        return coordinationConfig.getServices().values().stream()
                .filter(s -> userServicesCodes.contains(s.getCode()))
                .map(RscKpiReportMapper::toDto)
                .sorted(Comparator.comparing(UserServiceDto::getName))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public byte[] generateRscKpiExcelReport(RscKpiReportSubmittedFormDataDto submittedFormDataDto) throws IOException {
        return excelDataProcessor.generateRscKpiExcelReport(getRscKpiReportDataForExcelExportReport(submittedFormDataDto));
    }

    public String generateReportFileName(RscKpiReportSubmittedFormDataDto submittedFormDataDto, ReportTypeEnum reportTypeEnum) {
        StringBuilder fileNameBuilder = new StringBuilder();
        fileNameBuilder.append(generateReportFileNameWithoutExtension(submittedFormDataDto))
                .append(".")
                .append(reportTypeEnum == ReportTypeEnum.EXCEL ? "xlsx" : "pdf");
        return fileNameBuilder.toString();
    }

    protected String generateReportFileNameWithoutExtension(RscKpiReportSubmittedFormDataDto submittedFormDataDto) {
        if(submittedFormDataDto == null)
            return null;
        StringBuilder fileNameBuilder = new StringBuilder();
        CoordinationConfig.Service service = coordinationConfig.getServiceByCode(submittedFormDataDto.getRscServiceCode());
        fileNameBuilder.append(StringUtil.toCamelCase(service != null ? service.getName() : submittedFormDataDto.getRscServiceCode()))
                .append("_")
                .append(submittedFormDataDto.getKpiDataTypeCode().toLowerCase().concat("Kpis"))
                .append("_")
                .append(getSelectedEntitiesForExportFileName(submittedFormDataDto))
                .append("_")
                .append(submittedFormDataDto.getDataGranularity() == DataGranularityEnum.DAILY ? DateTimeFormatter.BASIC_ISO_DATE.format(submittedFormDataDto.getStartDate()) : submittedFormDataDto.getStartDate().getYear())
                .append("_")
                .append(submittedFormDataDto.getDataGranularity() == DataGranularityEnum.DAILY ? DateTimeFormatter.BASIC_ISO_DATE.format(submittedFormDataDto.getEndDate()) : submittedFormDataDto.getEndDate().getYear());
        return fileNameBuilder.toString();
    }

    protected String getSelectedEntitiesForExportFileName(RscKpiReportSubmittedFormDataDto submittedFormDataDto) {
        if (submittedFormDataDto != null) {
            if (CollectionUtils.isNotEmpty(submittedFormDataDto.getRscCodes())) {
                if (submittedFormDataDto.getRscCodes().size() == 1)
                    return Constants.ALL_RSCS_CODE.equals(submittedFormDataDto.getRscCodes().get(0))
                            ? StringUtil.toCamelCase(Constants.ALL_RSCS_NAME)
                            : StringUtil.toCamelCase(coordinationConfig.getRscByEicCode(submittedFormDataDto.getRscCodes().get(0)).getShortName()) + Constants.STRING_RSC;
                else
                    return submittedFormDataDto.getRscCodes().size() + Constants.STRING_RSCS;
            } else if (CollectionUtils.isNotEmpty(submittedFormDataDto.getRegionCodes())) {
                if (submittedFormDataDto.getRegionCodes().size() == 1)
                    return Constants.ALL_REGIONS_CODE.equals(submittedFormDataDto.getRegionCodes().get(0))
                            ? StringUtil.toCamelCase(Constants.ALL_REGIONS_NAME)
                            : StringUtil.toCamelCase(coordinationConfig.getRegionByEicCode(submittedFormDataDto.getRegionCodes().get(0)).getShortName()) + Constants.STRING_REGION;
                else
                    return submittedFormDataDto.getRegionCodes().size() + Constants.STRING_REGIONS;
            }
        }
        return "";
    }

}
