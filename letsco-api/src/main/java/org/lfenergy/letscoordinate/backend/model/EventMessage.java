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
import org.lfenergy.letscoordinate.backend.enums.CoordinationStatusEnum;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "event_message", catalog = "letsco")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class EventMessage implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "message_id", nullable = false, length = 100)
    private String messageId;

    @Column(name = "noun", nullable = false, length = 250)
    private String noun;

    @Column(name = "verb", nullable = false, length = 100)
    private String verb;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "source", nullable = false, length = 250)
    private String source;

    @Column(name = "format", nullable = false, length = 10)
    private String format;

    @Column(name = "business_application", length = 250)
    private String businessApplication;

    @Column(name = "message_type", nullable = false, length = 100)
    private String messageType;

    @Column(name = "message_type_name", nullable = false, length = 250)
    private String messageTypeName;

    @Column(name = "business_day_from", nullable = false)
    private Instant businessDayFrom;

    @Column(name = "business_day_to")
    private Instant businessDayTo;

    @Column(name = "process_step", length = 100)
    private String processStep;

    @Column(name = "timeframe", length = 50)
    private String timeframe;

    @Column(name = "timeframe_number")
    private Integer timeframeNumber;

    @Column(name = "sending_user", length = 50)
    private String sendingUser;

    @Column(name = "file_name", length = 100)
    private String fileName;

    @Column(name = "tso", length = 20)
    private String tso;

    @Column(name = "bidding_zone", length = 20)
    private String biddingZone;

    @Enumerated(EnumType.STRING)
    @Column(name = "coordination_status", length = 50)
    private CoordinationStatusEnum coordinationStatus;

    @Column(name = "case_id", length = 1000)
    private String caseId;

    @Column(name = "unique_file_identifier", length = 36)
    private String uniqueFileIdentifier;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "eventMessage", cascade = CascadeType.ALL)
    private List<EventMessageRecipient> eventMessageRecipients = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "eventMessage", cascade = CascadeType.ALL)
    private List<EventMessageCoordinationComment> eventMessageCoordinationComments = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "eventMessage", cascade = CascadeType.ALL)
    private List<EventMessageFile> eventMessageFiles = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "eventMessage", cascade = CascadeType.ALL)
    private List<Text> texts = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "eventMessage", cascade = CascadeType.ALL)
    private List<Link> links = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "eventMessage", cascade = CascadeType.ALL)
    private List<RscKpi> rscKpis = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "eventMessage", cascade = CascadeType.ALL)
    private List<Timeserie> timeseries = new ArrayList<>();

}
