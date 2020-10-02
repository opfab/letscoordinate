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

package org.lfenergy.letscoordinate.backend.repository;

import org.lfenergy.letscoordinate.backend.model.RscKpi;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RscKpiRepository extends CrudRepository<RscKpi, Long> {
    @Query(value = "select distinct k.* from rsc_kpi k " +
            "join event_message e on e.id = k.id_event_message " +
            "join rsc_kpi_data d on d.id_rsc_kpi = k.id " +
            "join rsc_kpi_data_details dd on dd.id_rsc_kpi_data = d.id " +
            "where d.timestamp >= :startDate and d.timestamp <= :endDate " +
            "and UPPER(k.name) like CONCAT('%',UPPER(:kpiDataTypeCode),'%') " +
            "and e.source like CONCAT('%',:rscServiceCode,'%') " +
            "and ( :rscCode = '' or dd.eic_code like CONCAT('%',:rscCode,'%')) ", nativeQuery = true)
    List<RscKpi> findReportingKpis(@Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate,
                                   @Param("kpiDataTypeCode") String kpiDataTypeCode,
                                   @Param("rscServiceCode") String rscServiceCode,
                                   @Param("rscCode") String rscCode);
}
