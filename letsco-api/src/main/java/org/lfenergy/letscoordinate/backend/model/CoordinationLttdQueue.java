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

package org.lfenergy.letscoordinate.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "coordination_lttd_queue", catalog = "letsco")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = {"coordination"})
@EqualsAndHashCode
public class CoordinationLttdQueue implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_coordination", nullable = false)
    @JsonBackReference
    private Coordination coordination;

    @Column(name = "lttd", nullable = false)
    private Instant lttd;

}
