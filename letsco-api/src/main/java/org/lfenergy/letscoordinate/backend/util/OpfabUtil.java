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

package org.lfenergy.letscoordinate.backend.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpfabUtil {

    public static String generateProcessKey(EventMessageDto eventMessageDto, boolean toLowerCaseIdentifier) {
        String source = eventMessageDto.getHeader().getSource();
        String messageTypeName = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getMessageTypeName();
        if (toLowerCaseIdentifier) {
            return new StringBuilder().append(StringUtil.toLowercaseIdentifier(source))
                    .append("_")
                    .append(StringUtil.toLowercaseIdentifier(messageTypeName))
                    .toString();
        } else {
            return source + "_" + messageTypeName;
        }
    }
}
