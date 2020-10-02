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
    GP1, GP2, GP3, GP4, GP5, GP6, GP7, GP8, GP9, GP10,
    GP11, GP12, GP13, GP14, GP15, GP16, GP17, GP18, GP19, GP20,
    GP21, GP22, GP23, GP24, GP25, GP26, GP27, GP28, GP29, GP30,
    GP31, GP32, GP33, GP34, GP35, GP36, GP37, GP38, GP39, GP40,
    GP41, GP42, GP43, GP44, GP45, GP46, GP47, GP48, GP49, GP50,
    GP51, GP52, GP53, GP54, GP55, GP56, GP57, GP58, GP59, GP60,
    GP61, GP62, GP63, GP64, GP65, GP66, GP67, GP68, GP69, GP70,
    GP71, GP72, GP73, GP74, GP75, GP76, GP77, GP78, GP79, GP80,
    GP81, GP82, GP83, GP84, GP85, GP86, GP87, GP88, GP89, GP90,
    GP91, GP92, GP93, GP94, GP95, GP96, GP97, GP98, GP99,

    BP1, BP2, BP3, BP4, BP5, BP6, BP7, BP8, BP9, BP10,
    BP11, BP12, BP13, BP14, BP15, BP16, BP17, BP18, BP19, BP20,
    BP21, BP22, BP23, BP24, BP25, BP26, BP27, BP28, BP29, BP30,
    BP31, BP32, BP33, BP34, BP35, BP36, BP37, BP38, BP39, BP40,
    BP41, BP42, BP43, BP44, BP45, BP46, BP47, BP48, BP49, BP50,
    BP51, BP52, BP53, BP54, BP55, BP56, BP57, BP58, BP59, BP60,
    BP61, BP62, BP63, BP64, BP65, BP66, BP67, BP68, BP69, BP70,
    BP71, BP72, BP73, BP74, BP75, BP76, BP77, BP78, BP79, BP80,
    BP81, BP82, BP83, BP84, BP85, BP86, BP87, BP88, BP89, BP90,
    BP91, BP92, BP93, BP94, BP95, BP96, BP97, BP98, BP99,

    UNKNOWN;

    public static KpiDataSubtypeEnum getByNameIgnoreCase (String name) {
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
