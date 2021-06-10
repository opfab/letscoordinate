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

package org.lfenergy.letscoordinate.backend.repository;

import org.lfenergy.letscoordinate.backend.model.CoordinationRaAnswer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface CoordinationRaAnswerRepository extends CrudRepository<CoordinationRaAnswer, Long> {
    void deleteByEicCodeAndCoordinationRa_Id(String eicCode, Long idCoordinationRa);

    @Query(value = "delete from coordination_ra_answer where eic_code = :eicCode and id_coordination_ra = :idCoordinationRa",
            nativeQuery = true)
    void deleteByEicCodeAndCoordinationRaId(String eicCode, Long idCoordinationRa);
}
