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

import java.util.Optional;
import java.util.stream.Stream;

public enum KpiDataSubtypeEnum {
    GP01, GP02, GP03, GP04, GP05, GP06, GP07, GP08, GP09, GP10,
    GP11, GP12, GP13, GP14, GP15, GP16, GP17, GP18, GP19, GP20,
    GP21, GP22, GP23, GP24, GP25, GP26, GP27, GP28, GP29, GP30,
    GP31, GP32, GP33, GP34, GP35, GP36, GP37, GP38, GP39, GP40,
    GP41, GP42, GP43, GP44, GP45, GP46, GP47, GP48, GP49, GP50,
    GP51, GP52, GP53, GP54, GP55, GP56, GP57, GP58, GP59, GP60,
    GP61, GP62, GP63, GP64, GP65, GP66, GP67, GP68, GP69, GP70,
    GP71, GP72, GP73, GP74, GP75, GP76, GP77, GP78, GP79, GP80,
    GP81, GP82, GP83, GP84, GP85, GP86, GP87, GP88, GP89, GP90,
    GP91, GP92, GP93, GP94, GP95, GP96, GP97, GP98, GP99,

    GP01_1, GP01_2, GP01_3, GP01_4, GP01_5, GP01_6, GP01_7, GP01_8, GP01_9,
    GP02_1, GP02_2, GP02_3, GP02_4, GP02_5, GP02_6, GP02_7, GP02_8, GP02_9,
    GP03_1, GP03_2, GP03_3, GP03_4, GP03_5, GP03_6, GP03_7, GP03_8, GP03_9,
    GP04_1, GP04_2, GP04_3, GP04_4, GP04_5, GP04_6, GP04_7, GP04_8, GP04_9,
    GP05_1, GP05_2, GP05_3, GP05_4, GP05_5, GP05_6, GP05_7, GP05_8, GP05_9,
    GP06_1, GP06_2, GP06_3, GP06_4, GP06_5, GP06_6, GP06_7, GP06_8, GP06_9,
    GP07_1, GP07_2, GP07_3, GP07_4, GP07_5, GP07_6, GP07_7, GP07_8, GP07_9,
    GP08_1, GP08_2, GP08_3, GP08_4, GP08_5, GP08_6, GP08_7, GP08_8, GP08_9,
    GP09_1, GP09_2, GP09_3, GP09_4, GP09_5, GP09_6, GP09_7, GP09_8, GP09_9,

    BP01, BP02, BP03, BP04, BP05, BP06, BP07, BP08, BP09, BP10,
    BP11, BP12, BP13, BP14, BP15, BP16, BP17, BP18, BP19, BP20,
    BP21, BP22, BP23, BP24, BP25, BP26, BP27, BP28, BP29, BP30,
    BP31, BP32, BP33, BP34, BP35, BP36, BP37, BP38, BP39, BP40,
    BP41, BP42, BP43, BP44, BP45, BP46, BP47, BP48, BP49, BP50,
    BP51, BP52, BP53, BP54, BP55, BP56, BP57, BP58, BP59, BP60,
    BP61, BP62, BP63, BP64, BP65, BP66, BP67, BP68, BP69, BP70,
    BP71, BP72, BP73, BP74, BP75, BP76, BP77, BP78, BP79, BP80,
    BP81, BP82, BP83, BP84, BP85, BP86, BP87, BP88, BP89, BP90,
    BP91, BP92, BP93, BP94, BP95, BP96, BP97, BP98, BP99,

    BP01_1, BP01_2, BP01_3, BP01_4, BP01_5, BP01_6, BP01_7, BP01_8, BP01_9,
    BP02_1, BP02_2, BP02_3, BP02_4, BP02_5, BP02_6, BP02_7, BP02_8, BP02_9,
    BP03_1, BP03_2, BP03_3, BP03_4, BP03_5, BP03_6, BP03_7, BP03_8, BP03_9,
    BP04_1, BP04_2, BP04_3, BP04_4, BP04_5, BP04_6, BP04_7, BP04_8, BP04_9,
    BP05_1, BP05_2, BP05_3, BP05_4, BP05_5, BP05_6, BP05_7, BP05_8, BP05_9,
    BP06_1, BP06_2, BP06_3, BP06_4, BP06_5, BP06_6, BP06_7, BP06_8, BP06_9,
    BP07_1, BP07_2, BP07_3, BP07_4, BP07_5, BP07_6, BP07_7, BP07_8, BP07_9,
    BP08_1, BP08_2, BP08_3, BP08_4, BP08_5, BP08_6, BP08_7, BP08_8, BP08_9,
    BP09_1, BP09_2, BP09_3, BP09_4, BP09_5, BP09_6, BP09_7, BP09_8, BP09_9,

    UNKNOWN;

    public static KpiDataSubtypeEnum getByNameIgnoreCase(String name) {
        return Optional.ofNullable(name)
                .map(n -> Stream.of(values())
                        .filter(v -> n.trim().toUpperCase().equals(v.name()))
                        .findFirst()
                        .orElse(UNKNOWN))
                .orElse(UNKNOWN);
    }

    public KpiDataTypeEnum getKpiDataType() {
        return this.name().startsWith(KpiDataTypeEnum.GP.name())
                ? KpiDataTypeEnum.GP
                : (this.name().startsWith(KpiDataTypeEnum.BP.name()) ? KpiDataTypeEnum.BP : KpiDataTypeEnum.UNKNOWN);
    }
}