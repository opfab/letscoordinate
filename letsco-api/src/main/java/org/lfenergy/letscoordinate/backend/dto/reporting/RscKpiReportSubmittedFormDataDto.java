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

import lombok.*;
import org.lfenergy.letscoordinate.backend.enums.DataGranularityEnum;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RscKpiReportSubmittedFormDataDto {
    DataGranularityEnum dataGranularity;
    LocalDate startDate;
    LocalDate endDate;
    List<String> rscCodes;
    List<String> regionCodes;
    String rscServiceCode;
    String kpiDataTypeCode;
}
