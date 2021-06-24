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
import org.lfenergy.letscoordinate.backend.enums.FileDirectionEnum;
import org.lfenergy.letscoordinate.backend.enums.FileTypeEnum;

import javax.persistence.*;
import java.time.Instant;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "event_message_file", catalog = "letsco")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = {"eventMessage"})
public class EventMessageFile implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_event_message", nullable = false)
    private EventMessage eventMessage;

    @Column(name = "file_name", nullable = false, length = 1000)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 20)
    private FileTypeEnum fileType;

    @Lob
    @Column(name = "file_content", nullable = false)
    private byte[] fileContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_direction", nullable = false, length = 20)
    private FileDirectionEnum fileDirection;

    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

}
