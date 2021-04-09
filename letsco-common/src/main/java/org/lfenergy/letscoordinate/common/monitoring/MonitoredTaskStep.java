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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MonitoredTaskStep {
    private TaskStepEnum step;
    private String context;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private TaskStatusEnum status;
    private String comment; // e.g: error message
    private String commentDetails; // e.g: full stackTrace

    @JsonIgnore
    public boolean isTerminated() {
        return status != null && endTime != null;
    }
}
