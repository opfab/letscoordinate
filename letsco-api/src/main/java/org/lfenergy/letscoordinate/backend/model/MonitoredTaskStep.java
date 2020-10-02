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

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "monitored_task_step", catalog = "letsco")
@Getter
@Setter
@Builder
@ToString(exclude = {"monitoredTask"})
public class MonitoredTaskStep implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_monitored_task", nullable = false)
    private MonitoredTask monitoredTask;

    @Column(name = "step", nullable = false, length = 50)
    private String step;

    @Column(name = "context", length = 65535)
    private String context;

    @Column(name = "start_time", nullable = false, length = 23)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false, length = 23)
    private LocalDateTime endTime;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "comment", length = 65535)
    private String comment;

    @Column(name = "comment_details", length = 65535)
    private String commentDetails;

}
