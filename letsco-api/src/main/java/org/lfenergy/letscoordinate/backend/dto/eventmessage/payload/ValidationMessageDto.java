/*
 * Copyright (c) 2020, RTE (https://www.rte-france.com)
 * Copyright (c) 2020 RTE international (https://www.rte-international.com)
 * See AUTHORS.txt
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 * This file is part of the Let’s Coordinate project.
 */

package org.lfenergy.letscoordinate.backend.dto.eventmessage.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.lfenergy.letscoordinate.backend.enums.ValidationSeverityEnum;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ValidationMessageDto {
    @NotNull
    private String code;
    @NotNull
    private ValidationSeverityEnum severity;
    @NotNull
    private String title;
    @NotNull
    private String message;
    @Getter(AccessLevel.NONE)
    private Map<String, Object> params;
    @NotNull
    private Instant businessTimestamp;
    private Map<String, Object> sourceDataRef;

    public Optional<Map<String, Object>> getParams() {
        return Optional.ofNullable(params);
    }

    @JsonProperty("params")
    public Map<String, Object> getParamsSimple() {
        return params;
    }
}
