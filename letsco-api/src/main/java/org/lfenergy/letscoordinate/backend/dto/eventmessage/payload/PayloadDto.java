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

package org.lfenergy.letscoordinate.backend.dto.eventmessage.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayloadDto {
    private List<TextDataDto> text;
    private List<LinkDataDto> links;
    private List<RscKpiDataDto> rscKpi;
    private List<TimeserieDataDto> timeserie;
    @Getter(AccessLevel.NONE)
    private ValidationDto validation;

    public Optional<ValidationDto> getValidation() {
        return Optional.ofNullable(validation);
    }

    @JsonProperty("validation")
    public ValidationDto getValidationSimple() {
        return validation;
    }
}
