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

import lombok.Getter;
import org.opfab.cards.model.SeverityEnum;

import java.util.Arrays;

import static org.opfab.cards.model.SeverityEnum.*;

@Getter
public enum BasicGenericNounEnum {

    PROCESS_SUCCESSFUL("ProcessSuccessful", "process successful", INFORMATION),
    PROCESS_FAILED("ProcessFailed", "process failed", ALARM),
    PROCESS_ACTION("ProcessAction", "process action", ACTION),
    PROCESS_INFORMATION("ProcessInformation", "process information", INFORMATION),
    MESSAGE_VALIDATED("DfgMessageValidated", "message validated", null),
    COORDINATION("Coordination", "coordination", ACTION);

    private String noun;
    private String titleProcessType;
    private SeverityEnum severity;

    BasicGenericNounEnum(String noun, String titleProcessType, SeverityEnum severity) {
        this.noun = noun;
        this.titleProcessType = titleProcessType;
        this.severity = severity;
    }

    public static BasicGenericNounEnum getByNoun(String noun) {
        return Arrays.stream(BasicGenericNounEnum.values()).filter(b -> b.getNoun().equals(noun)).findFirst()
                .orElse(null);
    }
}
