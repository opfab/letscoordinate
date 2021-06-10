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

import org.lfenergy.letscoordinate.backend.model.Coordination;
import org.lfenergy.letscoordinate.backend.model.CoordinationGeneralComment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface CoordinationGeneralCommentRepository extends CrudRepository<CoordinationGeneralComment, Long> {
    void deleteByEicCodeAndCoordination_Id(String eicCode, Long coordinationId);

    @Query(value = "delete from coordination_general_comment where eic_code = :eicCode and id_coordination = :coordinationId",
            nativeQuery = true)
    void deleteByEicCodeAndCoordinationId(String eicCode, Long coordinationId);
}
