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

package org.lfenergy.letscoordinate.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Validation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.letscoordinate.backend.component.OpfabPublisherComponent;
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.config.OpfabConfig;
import org.lfenergy.letscoordinate.backend.dto.coordination.CoordinationResponseDataDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.BusinessDataIdentifierDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.TimeserieDataDetailsDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.TimeserieTemporalDataDto;
import org.lfenergy.letscoordinate.backend.model.Coordination;
import org.lfenergy.letscoordinate.backend.model.CoordinationGeneralComment;
import org.lfenergy.letscoordinate.backend.model.CoordinationRa;
import org.lfenergy.letscoordinate.backend.model.CoordinationRaAnswer;
import org.lfenergy.letscoordinate.backend.repository.CoordinationGeneralCommentRepository;
import org.lfenergy.letscoordinate.backend.repository.CoordinationRaAnswerRepository;
import org.lfenergy.letscoordinate.backend.repository.CoordinationRepository;
import org.lfenergy.letscoordinate.backend.repository.EventMessageRepository;
import org.lfenergy.letscoordinate.backend.util.Constants;
import org.opfab.cards.model.Card;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.lfenergy.letscoordinate.backend.util.Constants.ENTITIES_REQUIRED_TO_RESPOND;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoordinationService {

    private final CoordinationRepository coordinationRepository;
    private final CoordinationRaAnswerRepository coordinationRaAnswerRepository;
    private final CoordinationGeneralCommentRepository coordinationGeneralCommentRepository;
    private final ObjectMapper objectMapper;
    private final EventMessageRepository eventMessageRepository;
    private final OpfabConfig opfabConfig;
    private final CoordinationConfig coordinationConfig;

    public Validation<Boolean, Coordination> saveAnswersAndCheckIfAllTsosHaveAnswered(Card card) {
        log.info("Card received:\n" + card.toString());
        saveAnswers(card);
        return checkIfAllTsosHaveAnswered(card);
    }

    public Coordination initAndSavaCoordination(Card card,
                                                EventMessageDto eventMessageDto,
                                                Long idEventMessage) {
        Instant timestamp = eventMessageDto.getHeader().getTimestamp();
        BusinessDataIdentifierDto businessDataIdentifierDto =
                eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        Instant businessDayFrom = businessDataIdentifierDto.getBusinessDayFrom();
        Instant businessDayTo = businessDataIdentifierDto.getBusinessDayTo();
        Coordination coordination = new Coordination();
        coordination.setEventMessage(eventMessageRepository.findById(idEventMessage).get());
        coordination.setProcessKey(card.getProcess());
        coordination.setPublishDate(timestamp);
        coordination.setStartDate(businessDayFrom.isBefore(timestamp) ? businessDayFrom : timestamp);
        coordination.setEndDate(businessDayTo);
        coordination.setStatus(null);
        coordination.setCoordinationRas(Optional.ofNullable(eventMessageDto.getPayload().getTimeserie())
                .map(timeseries -> timeseries.stream().findFirst()
                        .map(timeserie -> timeserie.getData().stream()
                                .map(datum -> fromTimeserieDataDetailsDto(datum, coordination))
                                .collect(Collectors.toList()))
                        .orElseGet(ArrayList::new))
                .orElseGet(ArrayList::new));
        return coordinationRepository.save(coordination);
    }

    public void saveAnswers(Card card) {
        String publisher = card.getPublisher();
        CoordinationResponseDataDto dataMap = objectMapper.convertValue(card.getData(), CoordinationResponseDataDto.class);
        coordinationRepository.findByEventMessage_Id(dataMap.getValidationDataId()).ifPresent(coordination -> {
            // save general comment
            coordinationGeneralCommentRepository.deleteByEicCodeAndCoordinationId(publisher, coordination.getId());
            coordinationGeneralCommentRepository.save(CoordinationGeneralComment.builder()
                    .coordination(coordination)
                    .eicCode(publisher)
                    .generalComment(dataMap.getGeneralComment())
                    .build());
            // save answers
            coordination.getCoordinationRas().stream()
                    .map(CoordinationRa::getCoordinationRaAnswers)
                    .flatMap(Collection::stream)
                    .filter(coordinationRaAnswer -> coordinationRaAnswer.getEicCode().equals(publisher))
                    .forEach(coordinationRaAnswer -> coordinationRaAnswerRepository.deleteByEicCodeAndCoordinationRaId(coordinationRaAnswer.getEicCode(), coordinationRaAnswer.getCoordinationRa().getId()));
            coordination.getCoordinationRas().forEach(coordinationRa -> {
                        CoordinationResponseDataDto.FormData newAnswer = dataMap.formDataMap().get(coordinationRa.getId());
                        if (newAnswer != null) {
                            coordinationRaAnswerRepository.save(CoordinationRaAnswer.builder()
                                    .coordinationRa(coordinationRa)
                                    .eicCode(publisher)
                                    .answer(newAnswer.getResponse())
                                    .explanation(newAnswer.getExplanation())
                                    .comment(newAnswer.getComment())
                                    .build());
                        }
                    });
        });
    }

    public Validation<Boolean, Coordination> checkIfAllTsosHaveAnswered(Card card) {
        CoordinationResponseDataDto dataMap = objectMapper.convertValue(card.getData(), CoordinationResponseDataDto.class);
        return coordinationRepository.findByEventMessage_Id(dataMap.getValidationDataId()).map(coordination -> {
            Map<String, List<String>> concernedEntitiesMap = OpfabPublisherComponent.getConcernedEntitiesMap(
                    coordination.getEventMessage(), opfabConfig, coordination.getProcessKey(), coordinationConfig.getTsos()
            );
            List<String> entitiesRequiredToRespond = concernedEntitiesMap.get(ENTITIES_REQUIRED_TO_RESPOND);
            List<String> entitiesResponded = coordination.getCoordinationRas().stream()
                    .map(CoordinationRa::getCoordinationRaAnswers)
                    .flatMap(Collection::stream)
                    .map(CoordinationRaAnswer::getEicCode)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (entitiesResponded.containsAll(entitiesRequiredToRespond)) {
                return Validation.<Boolean, Coordination>valid(coordination);
            }
            return Validation.<Boolean, Coordination>invalid(Boolean.TRUE);
        }).orElse(Validation.invalid(Boolean.TRUE));
    }

    protected CoordinationRa fromTimeserieDataDetailsDto(TimeserieDataDetailsDto timeserieDataDetailsDto,
                                                         Coordination coordination) {
        if (timeserieDataDetailsDto != null && timeserieDataDetailsDto.getDetail() != null) {
            Map<String, String> timeserieAsMap = timeserieDataDetailsDto.getDetail().stream()
                    .filter(d -> d.getLabel() != null)
                    .collect(Collectors.toMap(TimeserieTemporalDataDto::getLabel, TimeserieTemporalDataDto::getValue));
            return CoordinationRa.builder()
                    .coordination(coordination)
                    .event(timeserieAsMap.get(Constants.EVENT_KEY))
                    .constraintt(timeserieAsMap.get(Constants.CONSTRAINT_KEY))
                    .remedialAction(timeserieAsMap.get(Constants.REMEDIAL_ACTIONS_KEY))
                    .build();
        }
        return new CoordinationRa();
    }

}
