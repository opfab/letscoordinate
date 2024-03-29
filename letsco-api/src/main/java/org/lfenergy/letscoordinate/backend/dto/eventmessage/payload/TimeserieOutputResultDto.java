/*
 * Copyright (c) 2020-2021, RTE (https://www.rte-france.com)
 * Copyright (c) 2020-2021 RTE international (https://www.rte-international.com)
 * See AUTHORS.txt
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 * This file is part of the Let’s Coordinate project.
 */

package org.lfenergy.letscoordinate.backend.dto.eventmessage.payload;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.lfenergy.letscoordinate.backend.enums.CoordinationEntityGlobalResponseEnum;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
public class TimeserieOutputResultDto implements java.io.Serializable {
    @NotNull
    private String eicCode;
    @NotNull
    private CoordinationEntityGlobalResponseEnum answer;
    private String explanation;
    private String comment;
}
