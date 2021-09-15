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

package org.lfenergy.letscoordinate.backend.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    public static final String ROLE_PREFIX = "ROLE_";

    public static final String ALL_DATA_TYPE_CODE = "ALL";
    public static final String ALL_RSCS_CODE = "ALL_RSCS";
    public static final String ALL_RSCS_NAME = "Pan-EU";
    public static final String ALL_REGIONS_CODE = "ALL_REGIONS";
    public static final String ALL_REGIONS_NAME = "Pan-EU";
    public static final String STRING_RSC = "RSC";
    public static final String STRING_RSCS = "RSCs";
    public static final String STRING_REGION = "Region";
    public static final String STRING_REGIONS = "Regions";

    public static final String POSITIVE_ACK = "Positive";
    public static final String POSITIVE_ACK_WITH_WARNINGS = "Positive with warnings";
    public static final String NEGATIVE_ACK = "Negative";

    public static final String VALIDATION = "validation";
    public static final String PROCESS_MONITORING = "processmonitoring";

    public static final String EVENT_KEY = "Event";
    public static final String CONSTRAINT_KEY = "Constraint";
    public static final String REMEDIAL_ACTIONS_KEY = "Remedial actions";

    public static final String ENTITIES_RECIPIENTS = "ENTITIES_RECIPIENTS";
    public static final String ENTITIES_REQUIRED_TO_RESPOND = "ENTITIES_REQUIRED_TO_RESPOND";
    public static final String ENTITIES_ALLOWED_TO_RESPOND = "ENTITIES_ALLOWED_TO_RESPOND";

    public static final int NBR_EVENTS_TO_DISPLAY_IN_CARD_SUMMARY = 3;

    public static final String VISIBLE_CARD_TAG = "visible_card";
}
