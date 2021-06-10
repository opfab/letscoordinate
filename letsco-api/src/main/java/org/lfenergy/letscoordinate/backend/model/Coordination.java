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
import org.lfenergy.letscoordinate.backend.enums.CoordinationStatusEnum;

import javax.persistence.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "coordination", catalog = "letsco")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = {"eventMessage"})
public class Coordination implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_event_message", nullable = false)
    @JsonBackReference
    private EventMessage eventMessage;

    @Column(name = "process_key", nullable = false, length = 250)
    private String processKey;

    @Column(name = "publish_date", nullable = false)
    private Instant publishDate;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private CoordinationStatusEnum status;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "coordination", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<CoordinationGeneralComment> coordinationGeneralComments = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "coordination", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<CoordinationRa> coordinationRas = new ArrayList<>();

}
