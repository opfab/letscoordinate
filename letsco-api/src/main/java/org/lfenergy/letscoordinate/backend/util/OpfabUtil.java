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

package org.lfenergy.letscoordinate.backend.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.BusinessDataIdentifierDto;
import org.lfenergy.letscoordinate.backend.enums.CoordinationAnswerEnum;
import org.lfenergy.letscoordinate.backend.enums.OutputResultAnswerEnum;
import org.lfenergy.letscoordinate.backend.model.Coordination;
import org.lfenergy.letscoordinate.backend.model.CoordinationRa;
import org.lfenergy.letscoordinate.backend.model.CoordinationRaAnswer;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.lfenergy.letscoordinate.backend.util.Constants.NBR_EVENTS_TO_DISPLAY_IN_CARD_SUMMARY;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpfabUtil {

    public static String generateProcessKey(EventMessageDto eventMessageDto, boolean toLowerCaseIdentifier) {
        String source = eventMessageDto.getHeader().getSource();
        String messageTypeName = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getMessageTypeName();
        if (toLowerCaseIdentifier) {
            return new StringBuilder().append(StringUtil.toLowercaseIdentifier(source))
                    .append("_")
                    .append(StringUtil.toLowercaseIdentifier(messageTypeName))
                    .toString();
        } else {
            return source + "_" + messageTypeName;
        }
    }

    public static boolean isAgreementFound(Coordination coordination, List<String> concernedEntities) {
        return getCoordinationStatus(coordination, concernedEntities) == OutputResultAnswerEnum.CON;
    }

    public static OutputResultAnswerEnum getCoordinationStatus(Coordination coordination, List<String> concernedEntities) {
        if (coordination == null || CollectionUtils.isEmpty(coordination.getCoordinationRas()) || CollectionUtils.isEmpty(concernedEntities))
            return OutputResultAnswerEnum.NOT;
        int answersRequiredSize = coordination.getCoordinationRas().size() * concernedEntities.size();
        List<CoordinationRaAnswer> answers = coordination.getCoordinationRas().stream()
                .map(CoordinationRa::getCoordinationRaAnswers)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (answers.size() == answersRequiredSize) {
            if (answers.stream().allMatch(answer -> answer.getAnswer() == CoordinationAnswerEnum.OK))
                return OutputResultAnswerEnum.CON;
            else if (answers.stream().allMatch(answer -> answer.getAnswer() == CoordinationAnswerEnum.NOK))
                return OutputResultAnswerEnum.REJ;
            else
                return OutputResultAnswerEnum.MIX;
        } else if (answers.isEmpty()) {
            return OutputResultAnswerEnum.NOT;
        } else {
            return OutputResultAnswerEnum.MIX;
        }
    }

    public static String coordinationRasToString(Coordination coordination) {
        if (coordination == null || CollectionUtils.isEmpty(coordination.getCoordinationRas()))
            return "";
        StringBuilder builder = new StringBuilder();
        final int coordinationRasSize = coordination.getCoordinationRas().size();
        builder.append(coordination.getCoordinationRas().stream().limit(NBR_EVENTS_TO_DISPLAY_IN_CARD_SUMMARY)
                .map(CoordinationRa::getEvent).collect(Collectors.joining(", ")));
        builder.append(coordinationRasSize > NBR_EVENTS_TO_DISPLAY_IN_CARD_SUMMARY ? ", ... - " : " - ");
        builder.append(coordination.getCoordinationRas().stream().limit(NBR_EVENTS_TO_DISPLAY_IN_CARD_SUMMARY)
                .map(CoordinationRa::getConstraintt).collect(Collectors.joining(", ")));
        return builder.append(coordinationRasSize > NBR_EVENTS_TO_DISPLAY_IN_CARD_SUMMARY ? ", ..." : "").toString();
    }

    public static String generateCaseId(EventMessageDto eventMessageDto) {
        BusinessDataIdentifierDto bdi = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        return new StringBuilder().append(eventMessageDto.getHeader().getSource()).append("_")
                .append(bdi.getBusinessApplication().orElse(null)).append("_")
                .append(bdi.getMessageTypeName()).append("_")
                .append(bdi.getBusinessDayFrom()).append("_")
                .append(bdi.getBusinessDayTo()).toString();
    }
}
