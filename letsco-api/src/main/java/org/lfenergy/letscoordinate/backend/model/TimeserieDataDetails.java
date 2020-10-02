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
@Table(name = "timeserie_data_details", catalog = "letsco")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = {"timeserieData"})
public class TimeserieDataDetails implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_timeserie_data", nullable = false)
    private TimeserieData timeserieData;

    @Column(name = "identifier", length = 50)
    private String identifier;

    @Column(name = "label", length = 50)
    private String label;

    @Column(name = "value", nullable = false, length = 100)
    private String value;

    @Column(name = "accept", nullable = false)
    private Integer accept;

    @Column(name = "reject", nullable = false)
    private Integer reject;

    @Column(name = "explanation", nullable = false, length = 500)
    private String explanation;

    @Column(name = "comment", nullable = false, length = 500)
    private String comment;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "timeserieDataDetails", cascade = CascadeType.ALL)
    private List<TimeserieDataDetailsEicCode> timeserieDataDetailsEicCodes = new ArrayList<>();

}
