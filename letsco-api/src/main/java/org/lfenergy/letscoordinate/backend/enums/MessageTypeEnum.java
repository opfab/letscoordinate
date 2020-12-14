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

package org.lfenergy.letscoordinate.backend.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.operatorfabric.cards.model.SeverityEnum;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MessageTypeEnum {
    PROCESS_SUCCESSFUL("processsuccessful", "process successful", SeverityEnum.INFORMATION),
    PROCESS_FAILED("processfailed", "process failed", SeverityEnum.ALARM),
    POSITIVE_VALIDATION("positivevalidation", "positive validation", SeverityEnum.COMPLIANT),
    POSITIVE_VALIDATION_WITH_WARNINGS("positivevalidationwithwarnings", "positive validation with warnings", SeverityEnum.ACTION),
    NEGATIVE_VALIDATION("negativevalidation", "negative validation", SeverityEnum.ALARM);

    @Getter
    private String id;
    @Getter
    private String value;
    @Getter
    private SeverityEnum severity;


    public static MessageTypeEnum getById(String id) {
        if (StringUtils.isBlank(id))
            return null;
        for (MessageTypeEnum value : MessageTypeEnum.values()) {
            if (value.getId().equals(id))
                return value;
        }
        return null;
    }
}
