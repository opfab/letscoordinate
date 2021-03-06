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

import lombok.Getter;
import lombok.Setter;
import org.lfenergy.letscoordinate.backend.enums.DataGranularityEnum;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RscKpiDataDetailsDto implements IPayloadDataDetails {
    @NotNull
    private OffsetDateTime timestamp;
    @NotNull
    private DataGranularityEnum granularity;
    private String label;
    @NotNull
    @NotEmpty
    @Valid
    private List<RscKpiTemporalDataDto> detail = new ArrayList<>();
}
