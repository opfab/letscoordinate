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

package org.lfenergy.letscoordinate.common.monitoring;

public enum TaskStepEnum {
    STEP_DOWNLOAD_FILES,
    STEP_SAVE_FILE_DATA,
    STEP_MOVE_TREATED_FTP_FILE,
    STEP_MOVE_REJECTED_FTP_FILE,
    STEP_DELETE_LOCAL_FILE,
    STEP_ROLLBACK_SAVED_FILE_DATA
}
