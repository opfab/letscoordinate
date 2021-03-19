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

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "timeserie", catalog = "letsco")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = {"eventMessage"})
public class Timeserie implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_event_message", nullable = false)
    private EventMessage eventMessage;

    @Column(name = "name", nullable = false, length = 250)
    private String name;

    @Column(name = "coordination_status", length = 10)
    private String coordinationStatus;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "timeserie", cascade = CascadeType.ALL)
    private List<TimeserieData> timeserieDatas = new ArrayList<>();

}
