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

package org.lfenergy.letscoordinate.backend.controller;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.lfenergy.letscoordinate.backend.dto.reporting.RscKpiReportDataDto;
import org.lfenergy.letscoordinate.backend.dto.reporting.RscKpiReportSubmittedFormDataDto;
import org.lfenergy.letscoordinate.backend.enums.ReportTypeEnum;
import org.lfenergy.letscoordinate.backend.service.ReportingService;
import org.lfenergy.letscoordinate.backend.util.SecurityUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/letsco/api/v1")
@RequiredArgsConstructor
@Api(description = "Controller providing APIs to manage RSC KPIs Reporting")
public class RscKpiReportController {

    private final ReportingService reportingService;

    @GetMapping(value = "/rsc-kpi-report/config-data")
    @ApiOperation(value = "Get initial data for RSC KPIs form")
    @ApiImplicitParam(required = true, name = "Authorization", dataType = "string", paramType = "header")
    public ResponseEntity getRscKpiReportInitialFormData() {
        return reportingService.initRscKpiReportFormDto().fold(
                invalid -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(invalid),
                valid -> ResponseEntity.ok(valid));
    }

    @PostMapping(value = "/rsc-kpi-report/kpis")
    @ApiOperation(value = "Get RSCs with their services")
    @ApiImplicitParam(required = true, name = "Authorization", dataType = "string", paramType = "header")
    public ResponseEntity<RscKpiReportDataDto> getKpis(@RequestBody RscKpiReportSubmittedFormDataDto submittedFormDataDto) {
        return ResponseEntity.ok(reportingService.getRscKpiReportData(submittedFormDataDto));
    }

    @PostMapping(value = "/rsc-kpi-report/download/excel")
    @ApiOperation(value = "Download RSC KPI Excel report")
    @ApiImplicitParam(required = true, name = "Authorization", dataType = "string", paramType = "header")
    public ResponseEntity downloadRscKpiExcelReport(@RequestBody RscKpiReportSubmittedFormDataDto submittedFormDataDto) throws IOException {
        byte[] bytes = reportingService.generateRscKpiExcelReport(submittedFormDataDto);
        return ResponseEntity.ok()
                // Content-Disposition
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + reportingService.generateReportFileName(submittedFormDataDto, ReportTypeEnum.EXCEL))
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                // Content-Type
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                // Contet-Length
                .contentLength(bytes.length)
                .body(bytes);
    }

}
