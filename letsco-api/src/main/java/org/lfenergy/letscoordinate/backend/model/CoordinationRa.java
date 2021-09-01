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
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "coordination_ra", catalog = "letsco")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = {"coordination"})
@EqualsAndHashCode
public class CoordinationRa implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "id_timeserie_data", nullable = false)
    private Long idTimeserieData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_coordination", nullable = false)
    @JsonBackReference
    private Coordination coordination;

    @Column(name = "event", length = 500)
    private String event;

    @Column(name = "constraintt", length = 500)
    private String constraintt;

    @Column(name = "remedial_action", length = 500, nullable = false)
    private String remedialAction;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "coordinationRa", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<CoordinationRaAnswer> coordinationRaAnswers = new ArrayList<>();

}
