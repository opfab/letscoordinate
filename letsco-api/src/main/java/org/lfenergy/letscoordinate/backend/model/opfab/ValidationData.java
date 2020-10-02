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

package org.lfenergy.letscoordinate.backend.model.opfab;

import lombok.NoArgsConstructor;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.ValidationMessageDto;

import java.util.List;

@lombok.Data
@NoArgsConstructor
public class ValidationData extends EventMessageDto {

    List<ValidationMessageDto> warnings;
    List<ValidationMessageDto> errors;

    public ValidationData(EventMessageDto eventMessageDto) {
        super(eventMessageDto.getXmlns(), eventMessageDto.getHeader(), eventMessageDto.getPayload());
    }
}
