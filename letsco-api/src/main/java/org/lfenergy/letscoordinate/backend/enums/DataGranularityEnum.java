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

package org.lfenergy.letscoordinate.backend.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DataGranularityEnum {
    DAILY("D"), YEARLY("Y");

    @Getter
    @JsonValue
    private String value;

    public static DataGranularityEnum getByValue(String value) {
        if (value == null)
            return null;
        for (DataGranularityEnum dataGranularityEnum : values()) {
            if (dataGranularityEnum.getValue().equals(value))
                return dataGranularityEnum;
        }
        return null;
    }
}
