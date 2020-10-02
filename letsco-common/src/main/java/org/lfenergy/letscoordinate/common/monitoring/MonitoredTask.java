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
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MonitoredTask {
    private TaskEnum task;
    private String uuid;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<MonitoredTaskStep> monitoredTaskSteps;

    @JsonIgnore
    public boolean isTerminated() {
        return endTime != null;
    }
}
