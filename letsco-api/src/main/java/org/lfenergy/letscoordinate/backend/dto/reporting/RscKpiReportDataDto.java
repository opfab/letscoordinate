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
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.enums.KpiDataSubtypeEnum;
import org.lfenergy.letscoordinate.backend.enums.KpiDataTypeEnum;

import java.util.List;
import java.util.Map;

@Builder
@Getter
@Setter
public class RscKpiReportDataDto {
    private RscKpiReportSubmittedFormDataDto submittedFormData;
    private Map<KpiDataTypeEnum, Map<KpiDataSubtypeEnum, Map<String, List<RscKpiDto.DataDto>>>> rscKpiTypedDataMap;
    private Map<String, CoordinationConfig.KpiDataSubtype> rscKpiSubtypedDataMap;
}
