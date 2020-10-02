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

package org.lfenergy.letscoordinate.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "monitored_task", catalog = "letsco")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MonitoredTask implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "task", nullable = false, length = 50)
    private String task;

    @Column(name = "uuid", nullable = false, length = 36)
    private String uuid;

    @Column(name = "start_time", nullable = false, length = 23)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false, length = 23)
    private LocalDateTime endTime;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "monitoredTask", cascade = CascadeType.ALL)
    private List<MonitoredTaskStep> monitoredTaskSteps = new ArrayList<>();

}
