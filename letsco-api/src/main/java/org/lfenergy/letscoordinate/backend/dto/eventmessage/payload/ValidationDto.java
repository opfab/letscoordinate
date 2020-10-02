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

import lombok.*;
import org.lfenergy.letscoordinate.backend.enums.ValidationSeverityEnum;
import org.lfenergy.letscoordinate.backend.enums.ValidationStatusEnum;
import org.lfenergy.letscoordinate.backend.enums.ValidationTypeEnum;

import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationDto {

    private ValidationTypeEnum validationType;
    private ValidationStatusEnum status;
    private ValidationSeverityEnum result;
    @Getter(AccessLevel.NONE)
    private List<ValidationMessageDto> validationMessages;

    public Optional<List<ValidationMessageDto>> getValidationMessages() {
        return Optional.ofNullable(validationMessages);
    }
}
