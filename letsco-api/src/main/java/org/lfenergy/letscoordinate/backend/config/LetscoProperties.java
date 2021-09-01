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
import org.lfenergy.letscoordinate.backend.enums.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.ZoneId;
import java.util.*;

import static java.util.stream.Collectors.toList;

@ConfigurationProperties(prefix = "letsco")
@Getter
@Setter
public class LetscoProperties {

    private Kafka kafka;
    private ZoneId timezone = ZoneId.of("Europe/Paris");
    private InputFile inputFile;
    private Security security;
    private Coordination coordination;

    @Getter
    @Setter
    public static class Kafka {
        private String inputTopicPattern;
        private String defaultInputTopic;
        private String defaultOutputTopic;
    }

    @Getter
    @Setter
    public static class InputFile {
        private String dir;
        Map<BasicGenericNounEnum, List<String>> genericNouns = new HashMap<>();
        private Validation validation;

        @Getter
        @Setter
        public static class Validation {
            @Getter(AccessLevel.NONE)
            private List<String> ignoreProcesses = new ArrayList<>();
            @Getter(AccessLevel.NONE)
            private List<String> ignoreMessageTypeNames = new ArrayList<>();
            @Getter(AccessLevel.NONE)
            private Map<String, ChangeSource> changeSource;
            @Getter(AccessLevel.NONE)
            private Map<String, String> changeMessageTypeName = new HashMap<>();
            private boolean businessDayFromOptional;
            private boolean validationBusinessTimestampOptional;
            private boolean acceptPropertiesIgnoreCase;
            private boolean failOnUnknownProperties;
            private UnknownEicCodesProcessEnum unknownEicCodesProcess = UnknownEicCodesProcessEnum.EXCEPTION;
            @Getter(AccessLevel.NONE)
            private List<String> allowedEicCodes;

            public Optional<Map<String, ChangeSource>> getChangeSource() {
                return Optional.ofNullable(changeSource);
            }

            public Optional<List<String>> getIgnoreProcesses() {
                return Optional.ofNullable(ignoreProcesses);
            }

            public Optional<List<String>> getIgnoreMessageTypeNames() {
                return Optional.ofNullable(ignoreMessageTypeNames);
            }

            public Optional<Map<String, String>> getChangeMessageTypeName() {
                return Optional.ofNullable(changeMessageTypeName);
            }

            public Optional<List<String>> getAllowedEicCodes() {
                return Optional.ofNullable(allowedEicCodes);
            }
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ChangeSource {
            private ChangeJsonDataFromWhichEnum fromWhichLevel;
            private String changingField;
        }

        public List<String> allGenericNouns() {
            List<String> allGenericNouns =
                    genericNouns.values().stream().flatMap(Collection::stream).collect(toList());
            allGenericNouns.addAll(
                    Arrays.stream(BasicGenericNounEnum.values()).map(BasicGenericNounEnum::getNoun).collect(toList()));
            return allGenericNouns;
        }
    }

    @Getter
    @Setter
    public static class Security {
        private String[] allowedOrigins;
        private String clientId;
    }

    @Getter
    @Setter
    public static class Coordination {
        private CoordinationStatusStrategyEnum coordinationStatusCalculationStrategy;
        private Map<CoordinationStatusStrategyEnum, CoordinationStatusCalculationRule> coordinationStatusCalculationRules;
        private boolean notAnsweredDefaultCase;
        private Map<CoordinationStatusStrategyEnum, CoordinationStatusEnum> notAnsweredDefaultCaseRules;

        @Getter
        @Setter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class CoordinationStatusCalculationRule {
            private CoordinationStatusEnum conConCon;
            private CoordinationStatusEnum rejRejRej;
            private CoordinationStatusEnum notNotNot;
            private CoordinationStatusEnum mixMixMix;
            private CoordinationStatusEnum conConRej;
            private CoordinationStatusEnum conRejRej;
            private CoordinationStatusEnum conConMix;
            private CoordinationStatusEnum conRejMix;
            private CoordinationStatusEnum conConNot;
        }

        public CoordinationStatusCalculationRule getCoordinationStatusCalculationRule() {
            return coordinationStatusCalculationRules.get(coordinationStatusCalculationStrategy);
        }

        public CoordinationStatusEnum applyNotAnsweredDefaultValueIfNeeded(CoordinationStatusEnum coordinationStatusEnum) {
            return coordinationStatusEnum == CoordinationStatusEnum.NOT && notAnsweredDefaultCase ? getNotAnsweredDefaultValue() : coordinationStatusEnum;
        }

        private CoordinationStatusEnum getNotAnsweredDefaultValue() {
            return notAnsweredDefaultCaseRules.get(coordinationStatusCalculationStrategy);
        }
    }
}
