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

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.dto.reporting.*;
import org.lfenergy.letscoordinate.backend.enums.ReportTypeEnum;
import org.lfenergy.letscoordinate.backend.mapper.RscKpiReportMapper;
import org.lfenergy.letscoordinate.backend.model.RscKpi;
import org.lfenergy.letscoordinate.backend.processor.ExcelDataProcessor;
import org.lfenergy.letscoordinate.backend.repository.RscKpiRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportingService {

    private final CoordinationConfig coordinationConfig;
    private final RscKpiRepository rscKpiRepository;
    private final ExcelDataProcessor excelDataProcessor;

    public RscKpiReportInitialFormDataDto initRscKpiReportFormDto() {
        return RscKpiReportInitialFormDataDto.builder()
                .rscs(coordinationConfig.getRscs().values().stream()
                        .map(RscKpiReportMapper::toDto)
                        .sorted(Comparator.comparing(RscDto::getName))
                        .collect(Collectors.toList()))
                .rscServices(getCurrentUserRcsServices())
                .kpiDataTypes(coordinationConfig.getKpiDataTypes().values().stream()
                        .map(RscKpiReportMapper::toDto)
                        .sorted(Comparator.comparing(KpiDataTypeDto::getName))
                        .collect(Collectors.toList()))
                .build();
    }

    public RscKpiReportDataDto getRscKpiReportData(RscKpiReportSubmittedFormDataDto submittedFormDataDto) {
        List<RscKpi> rscKpis = rscKpiRepository.findReportingKpis(
                submittedFormDataDto.getStartDate(),
                submittedFormDataDto.getEndDate(),
                "all".equalsIgnoreCase(submittedFormDataDto.getKpiDataTypeCode()) ? "" : submittedFormDataDto.getKpiDataTypeCode(),
                submittedFormDataDto.getRscServiceCode(),
                "all".equalsIgnoreCase(submittedFormDataDto.getRscCode()) ? "" : submittedFormDataDto.getRscCode());
        return RscKpiReportDataDto.builder()
                .submittedFormData(submittedFormDataDto)
                .rscKpiTypedDataMap(RscKpiReportMapper.toMap(rscKpis, coordinationConfig.getKpiDataTypeMapByServiceCode(submittedFormDataDto.getRscServiceCode())))
                .rscKpiSubtypedDataMap(coordinationConfig.getKpiDataSubtypesByServiceCode(submittedFormDataDto.getRscServiceCode()))
                .build();
    }

    private List<RscServiceDto> getCurrentUserRcsServices() {
        // FIXME just for testing! to be changed by real currentUser's services from keyclock
        return coordinationConfig.getRscs().values().stream()
                .map(rsc -> rsc.getServices().values())
                .flatMap(Collection::stream)
                .collect(Collectors.toSet())
                .stream()
                .map(RscKpiReportMapper::toDto)
                .sorted(Comparator.comparing(RscServiceDto::getName))
                .collect(Collectors.toList());
    }

    public byte[] generateRscKpiExcelReport(RscKpiReportSubmittedFormDataDto submittedFormDataDto) throws IOException {
        return excelDataProcessor.generateRscKpiExcelReport(getRscKpiReportData(submittedFormDataDto));
    }

    public String generateReportFileName(RscKpiReportSubmittedFormDataDto submittedFormDataDto, ReportTypeEnum reportTypeEnum) {
        if(submittedFormDataDto == null)
            return null;
        StringBuilder fileNameBuilder = new StringBuilder();
        CoordinationConfig.Rsc.Service service = coordinationConfig.getServiceByCode(submittedFormDataDto.getRscServiceCode());
        CoordinationConfig.Rsc rsc = coordinationConfig.getRscByEicCode(submittedFormDataDto.getRscCode());
        fileNameBuilder.append(toCamelCase(service != null ? service.getName() : submittedFormDataDto.getRscServiceCode()))
                .append("_")
                .append(submittedFormDataDto.getKpiDataTypeCode().toLowerCase().concat("Kpis"))
                .append("_")
                .append("all".equalsIgnoreCase(submittedFormDataDto.getRscCode().toLowerCase()) ? "allRscs" : toCamelCase(rsc.getName()))
                .append("_")
                .append(DateTimeFormatter.BASIC_ISO_DATE.format(submittedFormDataDto.getStartDate()))
                .append("_")
                .append(DateTimeFormatter.BASIC_ISO_DATE.format(submittedFormDataDto.getEndDate()))
                .append(".")
                .append(reportTypeEnum == ReportTypeEnum.EXCEL ? "xlsx" : "pdf");
        return fileNameBuilder.toString();
    }

    private static String toCamelCase(String str) {
        if(StringUtils.isBlank(str))
            return str;
        String result = "";
        List<String> tokens = Arrays.asList(str.trim().split("[ _\\-]"));
        for(String token : tokens) {
            result += StringUtils.capitalize(token.toLowerCase());
        }
        return StringUtils.uncapitalize(result);
    }

    public static void main(String[] args) {
        System.out.println(toCamelCase("  AzErty wyEm-Fer_dawS "));
    }

}
