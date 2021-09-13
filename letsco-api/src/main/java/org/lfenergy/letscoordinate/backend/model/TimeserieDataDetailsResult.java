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

import lombok.*;
import org.lfenergy.letscoordinate.backend.enums.CoordinationEntityGlobalResponseEnum;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "timeserie_data_details_result", catalog = "letsco")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = {"timeserieDataDetails"})
public class TimeserieDataDetailsResult implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_timeserie_data_details", nullable = false)
    private TimeserieDataDetails timeserieDataDetails;

    @Column(name = "eic_code", nullable = false, length = 50)
    private String eicCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer", nullable = false, length = 50)
    private CoordinationEntityGlobalResponseEnum answer;

    @Column(name = "explanation", length = 1000)
    private String explanation;

    @Column(name = "comment", length = 1000)
    private String comment;

}
