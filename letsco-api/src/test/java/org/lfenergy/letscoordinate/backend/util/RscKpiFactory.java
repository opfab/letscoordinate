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
import org.lfenergy.letscoordinate.backend.enums.DataGranularityEnum;
import org.lfenergy.letscoordinate.backend.model.RscKpi;
import org.lfenergy.letscoordinate.backend.model.RscKpiData;
import org.lfenergy.letscoordinate.backend.model.RscKpiDataDetails;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RscKpiFactory {

    public static RscKpi createRscKpi() {
        return RscKpi.builder()
                .id(1L)
                .eventMessage(null)
                .name("GP01")
                .joinGraph(false)
                .rscKpiDatas(Arrays.asList(createRscKpiData()))
                .build();
    }

    public static RscKpiData createRscKpiData() {
        return RscKpiData.builder()
                .id(2L)
                .timestamp(OffsetDateTime.of(2021, 2, 8, 4, 0, 0, 0, ZoneOffset.UTC))
                .granularity(DataGranularityEnum.DAILY)
                .label("Global Perf 1")
                .rscKpiDataDetails(Arrays.asList(RscKpiDataDetails.builder()
                        .id(3L)
                        .eicCode("10XFR-RTE------Q")
                        .value(1L)
                        .build()
                ))
                .rscKpi(RscKpi.builder().id(1L).build())
                .build();
    }

}
