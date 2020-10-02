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

package org.lfenergy.letscoordinate.backend.config;

import lombok.*;
import org.lfenergy.letscoordinate.backend.enums.KpiDataSubtypeEnum;
import org.lfenergy.letscoordinate.backend.enums.KpiDataTypeEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@PropertySource("classpath:coordination.properties")
@ConfigurationProperties(prefix = "coordination")
@Getter
@Setter
public class CoordinationConfig {

    private Map<String, Tso> tsos;
    private Map<String, Rsc> rscs;
    private Map<String, KpiDataType> kpiDataTypes;
    private Map<String, Map<String, KpiDataSubtype>> kpiDataSubtypes;

    public Set<String> getRscEicCodes() {
        return Collections.unmodifiableSet(rscs.keySet());
    }

    public Set<String> getTsoEicCodes() {
        return Collections.unmodifiableSet(tsos.keySet());
    }

    public Set<String> getAllEicCodes() {
        return Collections.unmodifiableSet(Stream.of(getRscEicCodes(), getTsoEicCodes())
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()));
    }

    public Map<String, KpiDataSubtype> getKpiDataSubtypesByServiceCode(String serviceCode) {
        return kpiDataSubtypes.get(serviceCode);
    }

    public Map<KpiDataTypeEnum, List<KpiDataSubtypeEnum>> getKpiDataTypeMapByServiceCode(String serviceCode) {
        return getKpiDataSubtypesByServiceCode(serviceCode).keySet().stream()
                .sorted()
                .map(KpiDataSubtypeEnum::getByNameIgnoreCase)
                .collect(Collectors.groupingBy(KpiDataSubtypeEnum::getKpiDataType));
    }

    public Rsc getRscByEicCode(String eicCode) {
        return Optional.ofNullable(eicCode).map(rscs::get).orElse(null);
    }

    public Tso getTsoByEicCode(String eicCode) {
        return Optional.ofNullable(eicCode).map(tsos::get).orElse(null);
    }

    private Map<String, Rsc.Service> getServices() {
        return rscs.values().stream()
                .map(Rsc::getServices)
                .map(Map::values)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toMap(Rsc.Service::getCode, Function.identity()));
    }

    public Rsc.Service getServiceByCode(String code) {
        return Optional.ofNullable(code).map(c -> getServices().get(c)).orElse(null);
    }

    @Setter
    @Getter
    public static class Rsc {
        private String eicCode;
        private String name;
        private Map<String, Service> services;

        @Setter
        @Getter
        @EqualsAndHashCode
        public static class Service {
            private String code;
            private String name;
        }
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Tso {
        private String eicCode;
        private String name;
        private String rsc;
    }

    @Setter
    @Getter
    public static class KpiDataType {
        private String code;
        private String name;
    }

    @Setter
    @Getter
    public static class KpiDataSubtype {
        private String code;
        private String name;
        private String graphType;
    }

}
