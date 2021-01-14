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

import lombok.Getter;
import lombok.Setter;
import org.lfenergy.letscoordinate.backend.enums.UnknownEicCodesProcessEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

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
            private boolean acceptPropertiesIgnoreCase;
            private boolean failOnUnknownProperties;
            private UnknownEicCodesProcessEnum unknownEicCodesProcess = UnknownEicCodesProcessEnum.EXCEPTION;
            private List<String> allowedEicCodes = new ArrayList<>();
        }
    }
}
