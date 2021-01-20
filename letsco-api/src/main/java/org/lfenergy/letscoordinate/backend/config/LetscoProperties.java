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
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.ZoneId;

@ConfigurationProperties(prefix = "letsco")
@Getter
@Setter
public class LetscoProperties {
    private ZoneId timezone;
    private InputFile inputFile;
    private Security security;

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
        }
    }

    @Getter
    @Setter
    public static class Security {
        private String[] allowedOrigins;
        private String clientId;
    }
}
