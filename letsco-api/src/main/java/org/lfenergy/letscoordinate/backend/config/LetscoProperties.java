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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.lfenergy.letscoordinate.backend.enums.ChangeJsonDataFromWhichEnum;
import org.lfenergy.letscoordinate.backend.enums.UnknownEicCodesProcessEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

@ConfigurationProperties(prefix = "letsco")
@Getter
@Setter
public class LetscoProperties {

    private InputFile inputFile;

    @Getter
    @Setter
    public static class InputFile {
        private String dir;
        private Validation validation;

        @Getter
        @Setter
        public static class Validation {
            @Getter(AccessLevel.NONE)
            private List<String> ignoreProcesses = new ArrayList<>();
            @Getter(AccessLevel.NONE)
            private Map<String, ChangeSource> changeSource;
            @Getter(AccessLevel.NONE)
            private Map<String, String> changeMessageTypeName = new HashMap<>();
            private boolean businessDayFromOptional;
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

            public Optional<Map<String, String>> getChangeMessageTypeName() {
                return Optional.ofNullable(changeMessageTypeName);
            }

            public Optional<List<String>> getAllowedEicCodes() {
                return Optional.ofNullable(allowedEicCodes);
            }
        }

        @Getter
        @Setter
        public static class ChangeSource {
            private ChangeJsonDataFromWhichEnum fromWhichLevel;
            private String changingField;
        }
    }
}
