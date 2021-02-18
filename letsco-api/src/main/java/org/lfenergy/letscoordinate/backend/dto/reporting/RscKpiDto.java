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

package org.lfenergy.letscoordinate.backend.dto.reporting;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.lfenergy.letscoordinate.backend.enums.DataGranularityEnum;
import org.lfenergy.letscoordinate.backend.enums.KpiDataSubtypeEnum;
import org.lfenergy.letscoordinate.backend.enums.KpiDataTypeEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Builder
@Getter
@Setter
public class RscKpiDto {
    private String name;
    private Map<String, List<DataDto>> dataMap;

    @Builder
    @Getter
    @Setter
    public static class DataDto {
        private LocalDate timestamp;
        private DataGranularityEnum dataGranularity;
        private String label;
        private List<DetailsDto> details;

        @Builder
        @Getter
        @Setter
        public static class DetailsDto {
            private Long value;
            private String eicCode;
        }
    }

    public KpiDataTypeEnum getKpiDataType() {
        return Optional.ofNullable(name)
                .map(n -> n.trim().toUpperCase().startsWith(KpiDataTypeEnum.GP.name())
                        ? KpiDataTypeEnum.GP
                        : (n.trim().toUpperCase().startsWith(KpiDataTypeEnum.BP.name())
                            ? KpiDataTypeEnum.BP
                            : KpiDataTypeEnum.UNKNOWN))
                .orElse(KpiDataTypeEnum.UNKNOWN);
    }

    public KpiDataSubtypeEnum getKpiDataSubtype() {
        return Optional.ofNullable(name)
                .map(KpiDataSubtypeEnum::getByNameIgnoreCase)
                .orElse(KpiDataSubtypeEnum.UNKNOWN);
    }
}
