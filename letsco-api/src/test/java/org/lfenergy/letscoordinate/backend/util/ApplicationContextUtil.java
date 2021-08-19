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

package org.lfenergy.letscoordinate.backend.util;

import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.config.LetscoProperties;
import org.lfenergy.letscoordinate.backend.enums.CoordinationStatusEnum;
import org.lfenergy.letscoordinate.backend.enums.CoordinationStatusStrategyEnum;
import org.lfenergy.letscoordinate.backend.enums.LttdEnum;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lfenergy.letscoordinate.backend.enums.CoordinationStatusEnum.*;
import static org.lfenergy.letscoordinate.backend.enums.CoordinationStatusStrategyEnum.*;

public class ApplicationContextUtil {

    public static LetscoProperties initLetscoProperties() {
        LetscoProperties letscoProperties = new LetscoProperties();

        LetscoProperties.InputFile.Validation validation = new LetscoProperties.InputFile.Validation();
        validation.setAcceptPropertiesIgnoreCase(true);
        validation.setFailOnUnknownProperties(false);

        LetscoProperties.InputFile inputFile = new LetscoProperties.InputFile();
        inputFile.setDir("src/test/resources");
        inputFile.setValidation(validation);

        letscoProperties.setInputFile(inputFile);

        LetscoProperties.Coordination coordination = new LetscoProperties.Coordination();
        coordination.setLttd(new LetscoProperties.Coordination.Lttd());
        coordination.setCoordinationStatusCalculationStrategy(MAJORITY);
        Map<CoordinationStatusStrategyEnum, LetscoProperties.Coordination.CoordinationStatusCalculationRule> coordinationStatusCalculationRules = new HashMap<>();
        coordinationStatusCalculationRules.put(WORST_CASE,
                LetscoProperties.Coordination.CoordinationStatusCalculationRule.builder()
                        .conConCon(CON)
                        .rejRejRej(REJ)
                        .notNotNot(REJ)
                        .mixMixMix(MIX)
                        .conConRej(REJ)
                        .conRejRej(REJ)
                        .conConMix(MIX)
                        .conRejMix(REJ)
                        .conConNot(REJ)
                        .build());
        coordinationStatusCalculationRules.put(BEST_CASE,
                LetscoProperties.Coordination.CoordinationStatusCalculationRule.builder()
                        .conConCon(CON)
                        .rejRejRej(REJ)
                        .notNotNot(CON)
                        .mixMixMix(MIX)
                        .conConRej(CON)
                        .conRejRej(CON)
                        .conConMix(CON)
                        .conRejMix(CON)
                        .conConNot(CON)
                        .build());
        coordinationStatusCalculationRules.put(MAJORITY,
                LetscoProperties.Coordination.CoordinationStatusCalculationRule.builder()
                        .conConCon(CON)
                        .rejRejRej(REJ)
                        .notNotNot(NOT)
                        .mixMixMix(MIX)
                        .conConRej(CON)
                        .conRejRej(REJ)
                        .conConMix(CON)
                        .conRejMix(MIX)
                        .conConNot(CON)
                        .build());
        coordination.setCoordinationStatusCalculationRules(coordinationStatusCalculationRules);
        coordination.setNotAnsweredDefaultCase(true);
        Map<CoordinationStatusStrategyEnum, CoordinationStatusEnum> notAnsweredDefaultCaseRules = new HashMap<>();
        notAnsweredDefaultCaseRules.put(WORST_CASE, REJ);
        notAnsweredDefaultCaseRules.put(BEST_CASE, CON);
        notAnsweredDefaultCaseRules.put(MAJORITY, MIX);
        coordination.setNotAnsweredDefaultCaseRules(notAnsweredDefaultCaseRules);
        letscoProperties.setCoordination(coordination);

        LetscoProperties.Kafka kafka = new LetscoProperties.Kafka();
        kafka.setInputTopicPattern("letsco_eventmessage_input.*");
        kafka.setDefaultInputTopic("letsco_eventmessage_input");
        kafka.setDefaultOutputTopic("letsco_eventmessage_output");
        letscoProperties.setKafka(kafka);

        return letscoProperties;
    }

    public static CoordinationConfig initCoordinationConfig() {
        CoordinationConfig coordinationConfig = new CoordinationConfig();

        Map<String, CoordinationConfig.Tso> tsos = new HashMap<>();
        tsos.put("10X1001A1001A094", CoordinationConfig.Tso.builder().eicCode("10X1001A1001A094").name("Elia").build());
        tsos.put("10XCH-SWISSGRIDC", CoordinationConfig.Tso.builder().eicCode("10XCH-SWISSGRIDC").name("Swissgrid").build());
        tsos.put("10XFR-RTE------Q", CoordinationConfig.Tso.builder().eicCode("10XFR-RTE------Q").name("RTE").build());
        tsos.put("10XDE-VE-TRANSMK", CoordinationConfig.Tso.builder().eicCode("10XDE-VE-TRANSMK").name("50Hertz").build());
        tsos.put("10XCS-SERBIATSO8", CoordinationConfig.Tso.builder().eicCode("10XCS-SERBIATSO8").name("EMS").build());
        coordinationConfig.setTsos(tsos);

        Map<String, CoordinationConfig.Rsc> rscs = new HashMap<>();
        rscs.put("38X-BALTIC-RSC-H", CoordinationConfig.Rsc.builder().eicCode("38X-BALTIC-RSC-H").name("Baltic RSC").shortName("Baltic RSC").index(1).build());
        rscs.put("22XCORESO------S", CoordinationConfig.Rsc.builder().eicCode("22XCORESO------S").name("CORESO").shortName("CORESO").index(2).build());
        rscs.put("10X1001C--00008J", CoordinationConfig.Rsc.builder().eicCode("10X1001C--00008J").name("Nordic RSC").shortName("Nordic RSC").index(3).build());
        rscs.put("34X-0000000068-Q", CoordinationConfig.Rsc.builder().eicCode("34X-0000000068-Q").name("SCC").shortName("SCC").index(4).build());
        rscs.put("EICCODE-SELENECC", CoordinationConfig.Rsc.builder().eicCode("EICCODE-SELENECC").name("SEleNe CC").shortName("SEleNe CC").index(5).build());
        rscs.put("10X1001C--00003T", CoordinationConfig.Rsc.builder().eicCode("10X1001C--00003T").name("TSCNET").shortName("TSCNET").index(6).build());
        coordinationConfig.setRscs(rscs);

        Map<String, CoordinationConfig.Region> regions = new HashMap<>();
        regions.put("10Y1001C--00120B", CoordinationConfig.Region.builder().eicCode("10Y1001C--00120B").name("Baltic").shortName("Baltic").index(1).build());
        regions.put("10Y1001C--00095L", CoordinationConfig.Region.builder().eicCode("10Y1001C--00095L").name("South West Europe (SWE)").shortName("SWE").index(10).build());
        coordinationConfig.setRegions(regions);

        Map<String, CoordinationConfig.Service> services = new HashMap<>();
        services.put("SERVICE_A", CoordinationConfig.Service.builder().code("SERVICE_A").name("Service A").build());
        services.put("SERVICE_B", CoordinationConfig.Service.builder().code("SERVICE_B").name("Service B").build());
        coordinationConfig.setServices(services);

        Map<String, CoordinationConfig.KpiDataType> kpiDataTypes = new HashMap<>();
        kpiDataTypes.put("GP", CoordinationConfig.KpiDataType.builder().code("GP").name("Global performance KPIs").index(1).build());
        kpiDataTypes.put("BP", CoordinationConfig.KpiDataType.builder().code("BP").name("Business process KPIs").index(2).build());
        coordinationConfig.setKpiDataTypes(kpiDataTypes);

        Map<String, Map<String, CoordinationConfig.KpiDataSubtype>> kpiDataSubtypes = new HashMap<>();
        kpiDataSubtypes.put("SERVICE_A", Stream.of(
                CoordinationConfig.KpiDataSubtype.builder().code("GP1").name("Global Perf 1").graphType("bar").build(),
                CoordinationConfig.KpiDataSubtype.builder().code("GP2").name("Global Perf 2").graphType("line").build(),
                CoordinationConfig.KpiDataSubtype.builder().code("BP1").name("Business Process 1").graphType("bar").build(),
                CoordinationConfig.KpiDataSubtype.builder().code("BP2").name("Business Process 2").graphType("line").build(),
                CoordinationConfig.KpiDataSubtype.builder().code("BP3").name("Business Process 3").graphType("bar").build()
        ).collect(Collectors.toMap(CoordinationConfig.KpiDataSubtype::getCode, Function.identity())));
        kpiDataSubtypes.put("SERVICE_B", Stream.of(
                CoordinationConfig.KpiDataSubtype.builder().code("GP1").name("Global Perf 1").graphType("bar").build(),
                CoordinationConfig.KpiDataSubtype.builder().code("GP2").name("Global Perf 2").graphType("bar").build(),
                CoordinationConfig.KpiDataSubtype.builder().code("GP3").name("Global Perf 3").graphType("line").build()
        ).collect(Collectors.toMap(CoordinationConfig.KpiDataSubtype::getCode, Function.identity())));
        coordinationConfig.setKpiDataSubtypes(kpiDataSubtypes);

        return coordinationConfig;
    }

    public static Authentication createTestAuthentication() {
        Jwt jwt = Jwt.withTokenValue("letsco")
                .header("typ", "JWT")
                .claim("sub", "user.test")
                .claim("groups", Collections.singletonList("SERVICE_A"))
                .claim("scope", "RSC TSO")
                .build();
        return new TestingAuthenticationToken(jwt, null, "ROLE_TSO", Constants.SERVICE_PREFIX + "SERVICE_A", Constants.SERVICE_PREFIX + "SERVICE_B");
    }

}
