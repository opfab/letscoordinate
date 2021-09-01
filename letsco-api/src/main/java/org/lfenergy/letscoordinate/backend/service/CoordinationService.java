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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.letscoordinate.backend.component.OpfabPublisherComponent;
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.config.LetscoProperties;
import org.lfenergy.letscoordinate.backend.config.OpfabConfig;
import org.lfenergy.letscoordinate.backend.dto.KafkaFileWrapperDto;
import org.lfenergy.letscoordinate.backend.dto.coordination.CoordinationResponseDataDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageWrapperDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.BusinessDataIdentifierDto;
import org.lfenergy.letscoordinate.backend.enums.*;
import org.lfenergy.letscoordinate.backend.kafka.LetscoKafkaProducer;
import org.lfenergy.letscoordinate.backend.mapper.EventMessageMapper;
import org.lfenergy.letscoordinate.backend.model.*;
import org.lfenergy.letscoordinate.backend.processor.ExcelDataProcessor;
import org.lfenergy.letscoordinate.backend.repository.CoordinationGeneralCommentRepository;
import org.lfenergy.letscoordinate.backend.repository.CoordinationRaAnswerRepository;
import org.lfenergy.letscoordinate.backend.repository.CoordinationRepository;
import org.lfenergy.letscoordinate.backend.repository.EventMessageRepository;
import org.lfenergy.letscoordinate.backend.util.Constants;
import org.lfenergy.letscoordinate.backend.util.HttpUtil;
import org.opfab.cards.model.Card;
import org.opfab.cards.model.PublisherTypeEnum;
import org.opfab.cards.model.SeverityEnum;
import org.opfab.cards.model.TimeSpan;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
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
    private final LetscoKafkaProducer letscoKafkaProducer;
    private final ExcelDataProcessor excelDataProcessor;
    private final LetscoProperties letscoProperties;

    public Validation<Boolean, Coordination> saveAnswersAndCheckIfAllTsosHaveAnswered(Card card) {
        saveAnswers(card);
        return checkIfAllTsosHaveAnswered(card);
    }

    public Coordination initAndSaveCoordination(Card card,
                                                EventMessageDto eventMessageDto,
                                                Long idEventMessage) {
        Optional<EventMessage> eventMessageOptional = eventMessageRepository.findById(idEventMessage);
        if (eventMessageOptional.isPresent()) {
            EventMessage eventMessage = eventMessageOptional.get();
            Instant timestamp = eventMessage.getTimestamp();
            Instant businessDayFrom = eventMessage.getBusinessDayFrom();
            Instant businessDayTo = eventMessage.getBusinessDayTo();
            Coordination coordination = new Coordination();

            coordination.setEventMessage(eventMessage);
            coordination.setProcessKey(card.getProcess());
            coordination.setPublishDate(timestamp);
            coordination.setStartDate(businessDayFrom.isBefore(timestamp) ? businessDayFrom : timestamp);
            coordination.setEndDate(businessDayTo);
            coordination.setStatus(null);
            coordination.setCoordinationRas(Optional.ofNullable(eventMessage.getTimeseries())
                    .map(timeseries -> timeseries.stream().findFirst()
                            .map(timeserie -> timeserie.getTimeserieDatas().stream()
                                    .map(datum -> fromTimeserieData(datum, coordination))
                                    .collect(Collectors.toList()))
                            .orElseGet(ArrayList::new))
                    .orElseGet(ArrayList::new));
            return coordinationRepository.save(coordination);
        }
        return null;
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

    protected CoordinationRa fromTimeserieData(TimeserieData timeserieData,
                                               Coordination coordination) {
        if (timeserieData != null && timeserieData.getTimeserieDataDetailses() != null) {
            Map<String, String> timeserieAsMap = timeserieData.getTimeserieDataDetailses().stream()
                    .filter(d -> d.getLabel() != null)
                    .collect(Collectors.toMap(TimeserieDataDetails::getLabel, TimeserieDataDetails::getValue));
            return CoordinationRa.builder()
                    .coordination(coordination)
                    .idTimeserieData(timeserieData.getId())
                    .event(timeserieAsMap.get(Constants.EVENT_KEY))
                    .constraintt(timeserieAsMap.get(Constants.CONSTRAINT_KEY))
                    .remedialAction(timeserieAsMap.get(Constants.REMEDIAL_ACTIONS_KEY))
                    .build();
        }
        return new CoordinationRa();
    }

    public boolean generateOutputFile(Coordination coordination) throws IOException {
        EventMessage updatedEventMessage = applyCoordinationAnswersToEventMessage(coordination);
        try {
            letscoKafkaProducer.sendFileToKafka(eventMessageOutputFileToKafkaFileWrapperDto(updatedEventMessage),
                    letscoProperties.getKafka().getDefaultOutputTopic());
        } catch (Exception e) {
            log.error("Error while sending data to kafka!", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public EventMessage applyCoordinationAnswersToEventMessage(Coordination coordination) throws IOException {
        EventMessage eventMessage = coordination.getEventMessage();

        // Apply general comments
        if (CollectionUtils.isNotEmpty(coordination.getCoordinationGeneralComments())) {
            eventMessage.setEventMessageCoordinationComments(new ArrayList<>());
            eventMessage.getEventMessageCoordinationComments().addAll(
                    coordination.getCoordinationGeneralComments().stream()
                            .filter(gc -> StringUtils.isNotBlank(gc.getGeneralComment()))
                            .map(cc -> EventMessageCoordinationComment.builder()
                                    .eventMessage(eventMessage)
                                    .eicCode(cc.getEicCode())
                                    .generalComment(cc.getGeneralComment())
                                    .build())
                            .collect(Collectors.toList())
            );
        }

        // Apply coordination status
        List<String> concernedEntities =  coordination.getCoordinationRas().stream()
                .map(CoordinationRa::getCoordinationRaAnswers)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(CoordinationRaAnswer::getEicCode)
                .distinct()
                .collect(Collectors.toList());
        eventMessage.setCoordinationStatus(calculateCoordinationStatus(coordination, concernedEntities));

        // Apply answers
        for (CoordinationRa ra : coordination.getCoordinationRas()) {
            if (CollectionUtils.isNotEmpty(eventMessage.getTimeseries())) {
                Timeserie timeserie = eventMessage.getTimeseries().get(0);
                for (TimeserieData timeserieData : timeserie.getTimeserieDatas()) {
                    if (timeserieData.getId().equals(ra.getIdTimeserieData())) {
                        for (TimeserieDataDetails timeserieDataDetails : timeserieData.getTimeserieDataDetailses()) {
                            if (Constants.REMEDIAL_ACTIONS_KEY.equals(timeserieDataDetails.getLabel())) {
                                ra.getCoordinationRaAnswers().forEach(answer ->
                                        timeserieDataDetails.getTimeserieDataDetailsResults().add(TimeserieDataDetailsResult.builder()
                                                .timeserieDataDetails(timeserieDataDetails)
                                                .eicCode(answer.getEicCode())
                                                .answer(answer.getAnswer() == CoordinationAnswerEnum.OK ? OutputResultAnswerEnum.CON : OutputResultAnswerEnum.REJ)
                                                .comment(StringUtils.isNotBlank(answer.getComment()) ? answer.getComment() : null)
                                                .explanation(StringUtils.isNotBlank(answer.getExplanation()) ? answer.getExplanation() : null)
                                                .build())
                                );
                            }
                        }
                        break;
                    }
                }
            }
        }

        createOrUpdateEventMessageOutputFile(eventMessage);

        return eventMessageRepository.save(eventMessage);
    }

    @Deprecated
    public EventMessage applyCoordinationAnswersToEventMessage(Long idCoordination) throws IOException {
        Optional<Coordination> coordination = coordinationRepository.findById(idCoordination);
        if (coordination.isPresent()) {
            return applyCoordinationAnswersToEventMessage(coordination.get());
        }
        return null;
    }

    public KafkaFileWrapperDto eventMessageOutputFileToKafkaFileWrapperDto(EventMessage eventMessage) {
        if (eventMessage == null || CollectionUtils.isEmpty(eventMessage.getEventMessageFiles()))
            return null;
        return eventMessage.getEventMessageFiles().stream()
                .filter(f -> f.getFileDirection() == FileDirectionEnum.OUTPUT)
                .findFirst()
                .map(eventMessageFile -> KafkaFileWrapperDto.builder()
                        .fileName(eventMessageFile.getFileName())
                        .fileType(eventMessageFile.getFileType())
                        .fileContent(eventMessageFile.getFileContent())
                        .build())
                .orElse(null);
    }

    private void createOrUpdateEventMessageOutputFile(EventMessage eventMessage) throws IOException {
        Optional<EventMessageFile> existentInputFile = eventMessage.getEventMessageFiles().stream()
                .filter(f -> f.getFileDirection() == FileDirectionEnum.INPUT)
                .findFirst();
        Optional<EventMessageFile> existentOutputFile = eventMessage.getEventMessageFiles().stream()
                .filter(f -> f.getFileDirection() == FileDirectionEnum.OUTPUT)
                .findFirst();
        EventMessageFile newOutputFile = new EventMessageFile();
        if (existentOutputFile.isPresent()) {
            eventMessage.getEventMessageFiles().remove(existentOutputFile.get());
            newOutputFile.setId(existentOutputFile.get().getId());
        }
        newOutputFile.setEventMessage(eventMessage);
        newOutputFile.setFileName(existentInputFile.map(EventMessageFile::getFileName)
                .orElse(eventMessage.getUniqueFileIdentifier()));
        FileTypeEnum fileTypeEnum = existentInputFile.map(EventMessageFile::getFileType)
                .orElse(FileTypeEnum.getByExtensionIgnoreCase(eventMessage.getFormat()));
        newOutputFile.setFileType(fileTypeEnum);
        newOutputFile.setFileDirection(FileDirectionEnum.OUTPUT);
        newOutputFile.setCreationDate(Instant.now());

        switch(fileTypeEnum) {
            case JSON:
                newOutputFile.setFileContent(generateEventMessageJsonOutputFileContent(eventMessage));
                break;
            case EXCEL:
                newOutputFile.setFileContent(generateEventMessageExcelOutputFileContent(eventMessage));
                break;
            default:
        }

        eventMessage.getEventMessageFiles().add(newOutputFile);
    }

    public byte[] generateEventMessageJsonOutputFileContent(EventMessage eventMessage) throws IOException {
        EventMessageDto eventMessageDto = EventMessageMapper.toDto(eventMessage);
        Optional<EventMessageFile> existentInputFile = eventMessage.getEventMessageFiles().stream()
                .filter(f -> f.getFileDirection() == FileDirectionEnum.INPUT)
                .findFirst();
        if (existentInputFile.isPresent()) {
            BusinessDataIdentifierDto bdi = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
            EventMessageWrapperDto originalEventMessageWrapperDto = objectMapper.readValue(existentInputFile.get().getFileContent(),
                    EventMessageWrapperDto.class);
            BusinessDataIdentifierDto newBdi = originalEventMessageWrapperDto.getEventMessage().getHeader().getProperties().getBusinessDataIdentifier();
            newBdi.setCoordinationStatus(bdi.getCoordinationStatusSimple());
            newBdi.setCoordinationComments(bdi.getCoordinationCommentsSimple());
            originalEventMessageWrapperDto.getEventMessage().getPayload().setTimeserie(eventMessageDto.getPayload().getTimeserie());
            return objectMapper.writeValueAsBytes(originalEventMessageWrapperDto);
        }
        return objectMapper.writeValueAsBytes(EventMessageWrapperDto.builder().eventMessage(eventMessageDto).build());
    }

    public byte[] generateEventMessageExcelOutputFileContent(EventMessage eventMessage) throws IOException {
        Optional<EventMessageFile> existentInputFile = eventMessage.getEventMessageFiles().stream()
                .filter(f -> f.getFileDirection() == FileDirectionEnum.INPUT)
                .findFirst();
        if (existentInputFile.isPresent()) {
            return excelDataProcessor.generateEventMessageOutputFile(existentInputFile.get().getFileContent(), eventMessage);
        }
        return null;
    }

    public Optional<EventMessageFile> getEventMessageFileIfExists(Long idEventMessage, FileDirectionEnum fileDirectionEnum) {
        return eventMessageRepository.findById(idEventMessage)
                .map(eventMessage -> eventMessage.getEventMessageFiles().stream()
                        .filter(f -> f.getFileDirection() == fileDirectionEnum)
                        .sorted(Comparator.comparing(EventMessageFile::getCreationDate).reversed())
                        .findFirst())
                .orElse(Optional.empty());
    }

    private CoordinationStatusEnum calculateCoordinationStatus (Coordination coordination, List<String> concernedEntities) {
        LetscoProperties.Coordination.CoordinationStatusCalculationRule rules = letscoProperties.getCoordination().getCoordinationStatusCalculationRule();

        Map<String, OutputResultAnswerEnum> entityAnswersMap = getEntityAnswersMap(coordination, concernedEntities);
        Map<OutputResultAnswerEnum, Long> countingMap = entityAnswersMap.values().stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        if (countingMap.keySet().size() == 1) {
            if (countingMap.keySet().toArray()[0] == OutputResultAnswerEnum.CON)
                return letscoProperties.getCoordination().applyNotAnsweredDefaultValueIfNeeded(rules.getConConCon());
            else if (countingMap.keySet().toArray()[0] == OutputResultAnswerEnum.REJ)
                return letscoProperties.getCoordination().applyNotAnsweredDefaultValueIfNeeded(rules.getRejRejRej());
            else if (countingMap.keySet().toArray()[0] == OutputResultAnswerEnum.MIX)
                return letscoProperties.getCoordination().applyNotAnsweredDefaultValueIfNeeded(rules.getMixMixMix());
            else
                return letscoProperties.getCoordination().applyNotAnsweredDefaultValueIfNeeded(rules.getNotNotNot());
        } else {
            if (countingMap.containsKey(OutputResultAnswerEnum.CON)) {
                if (countingMap.containsKey(OutputResultAnswerEnum.REJ)) {
                    if (countingMap.containsKey(OutputResultAnswerEnum.MIX)) {
                        return letscoProperties.getCoordination().applyNotAnsweredDefaultValueIfNeeded(rules.getConRejMix());
                    } else {
                        if (countingMap.get(OutputResultAnswerEnum.CON) > countingMap.get(OutputResultAnswerEnum.REJ))
                            return letscoProperties.getCoordination().applyNotAnsweredDefaultValueIfNeeded(rules.getConConRej());
                        else
                            return letscoProperties.getCoordination().applyNotAnsweredDefaultValueIfNeeded(rules.getConRejRej());
                    }
                } else {
                    if (countingMap.containsKey(OutputResultAnswerEnum.MIX)) {
                        return letscoProperties.getCoordination().applyNotAnsweredDefaultValueIfNeeded(rules.getConConMix());
                    } else {
                        return letscoProperties.getCoordination().applyNotAnsweredDefaultValueIfNeeded(rules.getConConNot());
                    }
                }
            } else {
                return letscoProperties.getCoordination().applyNotAnsweredDefaultValueIfNeeded(rules.getMixMixMix());
            }
        }
    }

    private Map<String, OutputResultAnswerEnum> getEntityAnswersMap(Coordination coordination, List<String> concernedEntities) {
        Map<String, List<CoordinationRaAnswer>> entityAnswersMap = coordination.getCoordinationRas().stream()
                .map(CoordinationRa::getCoordinationRaAnswers)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(CoordinationRaAnswer::getEicCode));
        Map<String, OutputResultAnswerEnum> result = new HashMap<>();
        for (String entity: concernedEntities) {
            if (entityAnswersMap.containsKey(entity)) {
                Set<CoordinationAnswerEnum> answers = entityAnswersMap.get(entity).stream()
                        .map(CoordinationRaAnswer::getAnswer)
                        .collect(Collectors.toSet());
                if (answers.isEmpty())
                    result.put(entity, OutputResultAnswerEnum.NOT);
                else if (answers.size() > 1)
                    result.put(entity, OutputResultAnswerEnum.MIX);
                else if (answers.toArray()[0] == CoordinationAnswerEnum.OK)
                    result.put(entity, OutputResultAnswerEnum.CON);
                else
                    result.put(entity, OutputResultAnswerEnum.REJ);
            } else {
                result.put(entity, OutputResultAnswerEnum.NOT);
            }
        }
        return result;
    }

    // COORDINATION FILE
    public void sendCoordinationFileCard(Card coordinationCard, FileDirectionEnum fileDirectionEnum) {
        try {
            Card card = new Card();
            card.setProcessVersion("1");
            card.setPublisher(opfabConfig.getPublisher());
            card.setTitle(coordinationCard.getTitle());
            card.setSummary(coordinationCard.getSummary());
            String stateId = fileDirectionEnum == FileDirectionEnum.INPUT ? "inputFile" : "outputFile";
            card.setProcess(coordinationCard.getProcess() + "_file");
            card.setProcessInstanceId(coordinationCard.getProcessInstanceId() + "_" + stateId);
            card.setSeverity(SeverityEnum.ACTION);
            card.setState(stateId);
            card.setKeepChildCards(false);
            card.setPublisherType(PublisherTypeEnum.EXTERNAL);
            card.setGroupRecipients(coordinationCard.getGroupRecipients());
            card.setEntityRecipients(coordinationCard.getEntityRecipients());
            card.setTimeSpans(null);
            card.setStartDate(coordinationCard.getStartDate());
            card.setEndDate(coordinationCard.getEndDate());
            Map<String, Object> data = new HashMap<>();
            data.put("businessDayFrom", coordinationCard.getStartDate());
            data.put("businessDayTo", coordinationCard.getEndDate());
            data.put("isInputFile", fileDirectionEnum == FileDirectionEnum.INPUT);
            card.setData(data);
            log.info("Opfab {} file notification generated", fileDirectionEnum);
            HttpUtil.post(opfabConfig.getUrl().getCardsPub(), card);
        } catch (Exception e) {
            log.error("Unable to send coordination file notification!", e);
        }
    }

}
