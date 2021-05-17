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

package org.lfenergy.letscoordinate.backend.dto.eventmessage.header;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.lfenergy.letscoordinate.backend.enums.CoordinationStatusEnum;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class BusinessDataIdentifierDto {

    @Getter(AccessLevel.NONE)
    private List<String> recipients;
    private String messageType;
    @NotNull
    private String messageTypeName;
    @NotNull
    private Instant businessDayFrom;
    private Instant businessDayTo;
    @Getter(AccessLevel.NONE)
    private String processStep;
    @Getter(AccessLevel.NONE)
    private String timeframe;
    @Getter(AccessLevel.NONE)
    private String businessApplication;
    @Getter(AccessLevel.NONE)
    private Integer timeframeNumber;
    @Getter(AccessLevel.NONE)
    private String sendingUser;
    @Getter(AccessLevel.NONE)
    private String fileName;
    @Getter(AccessLevel.NONE)
    private String tso;
    @Getter(AccessLevel.NONE)
    private String biddingZone;
    @Getter(AccessLevel.NONE)
    private CoordinationStatusEnum coordinationStatus;
    @Getter(AccessLevel.NONE)
    @Valid
    private List<CoordinationCommentDto> coordinationComments;

    public Optional<String> getProcessStep() {
        return Optional.ofNullable(processStep);
    }

    @JsonProperty("processStep")
    public String getProcessStepSimple() {
        return processStep;
    }

    public Optional<List<String>> getRecipients() {
        return Optional.ofNullable(recipients);
    }

    public Optional<String> getTimeframe() {
        return Optional.ofNullable(timeframe);
    }

    @JsonProperty("timeframe")
    public String getTimeframeSimple() {
        return timeframe;
    }

    public Optional<Integer> getTimeframeNumber() {
        return Optional.ofNullable(timeframeNumber);
    }

    @JsonProperty("timeframeNumber")
    public Integer getTimeframeNumberSimple() {
        return timeframeNumber;
    }

    public Optional<String> getSendingUser() {
        return Optional.ofNullable(sendingUser);
    }

    @JsonProperty("sendingUser")
    public String getSendingUserSimple() {
        return sendingUser;
    }

    public Optional<String> getFileName() {
        return Optional.ofNullable(fileName);
    }

    @JsonProperty("fileName")
    public String getFileNameSimple() {
        return fileName;
    }

    public Optional<String> getTso() {
        return Optional.ofNullable(tso);
    }

    @JsonProperty("tso")
    public String getTsoSimple() {
        return tso;
    }

    public Optional<String> getBiddingZone() {
        return Optional.ofNullable(biddingZone);
    }

    @JsonProperty("biddingZone")
    public String getBiddingZoneSimple() {
        return biddingZone;
    }

    public Optional<String> getBusinessApplication() {
        return Optional.ofNullable(businessApplication);
    }

    @JsonProperty("businessApplication")
    public String getBusinessApplicationSimple() {
        return businessApplication;
    }

    public Optional<CoordinationStatusEnum> getCoordinationStatus() {
        return Optional.ofNullable(coordinationStatus);
    }

    @JsonProperty("coordinationStatus")
    public CoordinationStatusEnum getCoordinationStatusSimple() {
        return coordinationStatus;
    }

    public Optional<List<CoordinationCommentDto>> getCoordinationComments() {
        return Optional.ofNullable(coordinationComments);
    }

    @JsonProperty("coordinationComment")
    public List<CoordinationCommentDto> getCoordinationCommentsSimple() {
        return coordinationComments;
    }
}
