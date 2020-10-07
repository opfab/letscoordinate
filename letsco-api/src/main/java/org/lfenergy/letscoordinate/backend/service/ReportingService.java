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
import org.lfenergy.letscoordinate.backend.enums.ReportTypeEnum;
import org.lfenergy.letscoordinate.backend.mapper.RscKpiReportMapper;
import org.lfenergy.letscoordinate.backend.model.RscKpi;
import org.lfenergy.letscoordinate.backend.model.User;
import org.lfenergy.letscoordinate.backend.model.UserService;
import org.lfenergy.letscoordinate.backend.processor.ExcelDataProcessor;
import org.lfenergy.letscoordinate.backend.repository.RscKpiRepository;
import org.lfenergy.letscoordinate.backend.repository.UserRepository;
import org.lfenergy.letscoordinate.backend.util.SecurityUtil;
import org.lfenergy.letscoordinate.common.exception.AuthorizationException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportingService {

    private final CoordinationConfig coordinationConfig;
    private final RscKpiRepository rscKpiRepository;
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
                        .sorted(Comparator.comparing(RscDto::getName))
                        .collect(Collectors.toList()))
                .rscServices(getCurrentUserRcsServices(u.getUserServices()))
                .kpiDataTypes(coordinationConfig.getKpiDataTypes().values().stream()
                        .map(RscKpiReportMapper::toDto)
                        .sorted(Comparator.comparing(KpiDataTypeDto::getName))
                        .collect(Collectors.toList()))
                .build()))
                .orElseGet(() -> Validation.invalid("User \"" + username + "\" not found!"));
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

    public byte[] generateRscKpiExcelReport(RscKpiReportSubmittedFormDataDto submittedFormDataDto) throws IOException {
        return excelDataProcessor.generateRscKpiExcelReport(getRscKpiReportData(submittedFormDataDto));
    }

    public String generateReportFileName(RscKpiReportSubmittedFormDataDto submittedFormDataDto, ReportTypeEnum reportTypeEnum) {
        if(submittedFormDataDto == null)
            return null;
        StringBuilder fileNameBuilder = new StringBuilder();
        CoordinationConfig.Service service = coordinationConfig.getServiceByCode(submittedFormDataDto.getRscServiceCode());
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

}
