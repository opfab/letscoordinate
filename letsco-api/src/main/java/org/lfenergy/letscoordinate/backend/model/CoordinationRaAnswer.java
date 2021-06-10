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
import org.lfenergy.letscoordinate.backend.enums.CoordinationAnswerEnum;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "coordination_ra_answer", catalog = "letsco")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = {"coordinationRa"})
@EqualsAndHashCode
public class CoordinationRaAnswer implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_coordination_ra", nullable = false)
    @JsonBackReference
    private CoordinationRa coordinationRa;

    @Column(name = "eic_code", nullable = false, length = 20)
    private String eicCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer", nullable = false)
    private CoordinationAnswerEnum answer;

    @Column(name = "explanation", length = 500)
    private String explanation;

    @Column(name = "comment", length = 500)
    private String comment;

}
