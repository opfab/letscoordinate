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

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class TimeserieTemporalDataDto implements IPayloadTemporalData {

    public static String TIMESERIE_TEMPORAL_DATA_DEFAULT_VALUE = "!@#$%^&*(";

    // Common fields (Input and Output)
    private String id;
    private String label;
    private List<String> eicCode;
    private String value = TIMESERIE_TEMPORAL_DATA_DEFAULT_VALUE;
    // Output fields
    private Integer accept;
    private Integer reject;
    private String explanation;
    private String comment;
    // OpFab data field
    private Object opfabDataValue;
}
