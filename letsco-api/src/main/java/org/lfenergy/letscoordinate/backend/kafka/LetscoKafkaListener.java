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

package org.lfenergy.letscoordinate.backend.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Validation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.letscoordinate.backend.component.OpfabPublisherComponent;
import org.lfenergy.letscoordinate.backend.config.LetscoProperties;
import org.lfenergy.letscoordinate.backend.config.OpfabConfig;
import org.lfenergy.letscoordinate.backend.dto.ResponseErrorDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.BusinessDataIdentifierDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.HeaderDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.ValidationDto;
import org.lfenergy.letscoordinate.backend.enums.BasicGenericNounEnum;
import org.lfenergy.letscoordinate.backend.enums.ChangeJsonDataFromWhichEnum;
import org.lfenergy.letscoordinate.backend.enums.ValidationSeverityEnum;
import org.lfenergy.letscoordinate.backend.enums.ValidationTypeEnum;
import org.lfenergy.letscoordinate.backend.exception.IgnoreProcessException;
import org.lfenergy.letscoordinate.backend.exception.JsonDataMandatoryFieldNullException;
import org.lfenergy.letscoordinate.backend.exception.PositiveTechnicalQualityCheckException;
import org.lfenergy.letscoordinate.backend.mapper.EventMessageMapper;
import org.lfenergy.letscoordinate.backend.model.EventMessage;
import org.lfenergy.letscoordinate.backend.processor.JsonDataProcessor;
import org.lfenergy.letscoordinate.backend.repository.EventMessageRepository;
import org.lfenergy.letscoordinate.backend.util.HttpUtil;
import org.lfenergy.letscoordinate.backend.util.OpfabUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static org.lfenergy.letscoordinate.backend.enums.BasicGenericNounEnum.MESSAGE_VALIDATED;

@Component
@RequiredArgsConstructor
@Slf4j
public class LetscoKafkaListener {

    @Value("${third-app.url}")
    private String thirdAppUrl;

    private final JsonDataProcessor jsonDataProcessor;
    private final EventMessageRepository eventMessageRepository;
    private final OpfabPublisherComponent opfabPublisherComponent;
    private final LetscoProperties letscoProperties;
    private final OpfabConfig opfabConfig;

    @KafkaListener(topicPattern = "#{@kafkaTopicPattern}")
    public void handleLetscoEventMessages(@Payload String data,
                                          @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                          @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts) throws Exception {

        log.info("Data receiced from topic \"{}\" (kafka_receivedTimestamp = {}, kafka_receivedPartitionId = {})", topic,
                DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(ts)), partition);
        log.info("Received data: {}", data);

        Validation<ResponseErrorDto, EventMessageDto> validation = jsonDataProcessor.inputStreamToPojo(new ByteArrayInputStream(data.getBytes()));
        if (validation.isInvalid()) {
            log.error(validation.getError().toString());
            return;
        }
        EventMessageDto eventMessageDto = validation.get();

        String noun = eventMessageDto.getHeader().getNoun();
        if (!isGenericNoun(noun)) {
            String url = String.format("%s/api/json", thirdAppUrl);
            HttpUtil.post(url, data);
            return;
        }

        try {
            verifyData(eventMessageDto);

            log.info("Received data type: \"{}\"", eventMessageDto.getHeader().getNoun());
            EventMessage eventMessage = eventMessageRepository.save(EventMessageMapper.fromDto(eventMessageDto));
            log.info("New \"{}\" data successfully saved! (id={})", eventMessage.getNoun(), eventMessage.getId());
            log.debug("Saved data >>> {}", eventMessage.toString());

            opfabPublisherComponent.publishOpfabCard(eventMessageDto, eventMessage.getId());

        } catch (IgnoreProcessException | PositiveTechnicalQualityCheckException | JsonDataMandatoryFieldNullException e) {
            log.info(e.getMessage());
        }
    }

    void verifyData(EventMessageDto eventMessageDto) {
        BusinessDataIdentifierDto bdi = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        changeNounIfNeeded(eventMessageDto.getHeader());
        changeSourceIfNeeded(eventMessageDto);
        changeMessageTypeNameIfNeeded(bdi);
        String messageTypeName =
                eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getMessageTypeName();
        String processKey = OpfabUtil.generateProcessKey(eventMessageDto,
                opfabConfig.isProcessKeyToLowerCaseIdentifier());
        ignoreProcessIfNeeded(processKey);
        ignoreMessageTypeNameIfNeeded(messageTypeName);
        ignorePositiveTechnicalQualityCheck(eventMessageDto);
        processIfBusinessDayFromOptional(eventMessageDto);
        bdi.setBusinessDayTo(bdi.getBusinessDayTo() == null ?
                bdi.getBusinessDayFrom().plus(Duration.ofHours(24)).minus(Duration.ofSeconds(1)) :
                bdi.getBusinessDayTo().minus(Duration.ofSeconds(1)));
    }

    void ignoreMessageTypeNameIfNeeded(String messageTypeName) {
        letscoProperties.getInputFile().getValidation().getIgnoreMessageTypeNames().ifPresent(messageTypeNamesToIgnore -> {
            if (messageTypeNamesToIgnore.contains(messageTypeName)) {
                throw new IgnoreProcessException("Json message ignored. Message type name: " + messageTypeName);
            }
        });
    }

    void ignoreProcessIfNeeded(String process) {
        letscoProperties.getInputFile().getValidation().getIgnoreProcesses().ifPresent(processesToIgnore -> {
            if (processesToIgnore.contains(process)) {
                throw new IgnoreProcessException("Json message ignored. Process: " + process);
            }
        });
    }

    void ignorePositiveTechnicalQualityCheck(EventMessageDto eventMessageDto) {
        if (MESSAGE_VALIDATED.getNoun().equals(eventMessageDto.getHeader().getNoun())) {
            ValidationDto validationDto = eventMessageDto.getPayload().getValidation().get();
            if (validationDto.getResult() == ValidationSeverityEnum.OK &&
                    validationDto.getValidationType() == ValidationTypeEnum.TECHNICAL) {
                throw new PositiveTechnicalQualityCheckException("Positive technical quality check => no need to process it");
            }
        }
    }

    void processIfBusinessDayFromOptional(EventMessageDto eventMessageDto) {
        BusinessDataIdentifierDto bdi = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        Instant timestamp = eventMessageDto.getHeader().getTimestamp();
        if (letscoProperties.getInputFile().getValidation().isBusinessDayFromOptional()) {
            if (Objects.isNull(bdi.getBusinessDayFrom())) {
                bdi.setBusinessDayFrom(timestamp);
            }
        } else {
            if (bdi.getBusinessDayFrom() == null) {
                throw new JsonDataMandatoryFieldNullException("businessDayFrom");
            }
        }
    }

    void changeMessageTypeNameIfNeeded(BusinessDataIdentifierDto bdi) {
        String messageTypeName = bdi.getMessageTypeName();
        letscoProperties.getInputFile().getValidation().getChangeMessageTypeName().ifPresent(m -> {
            if (m.containsKey(messageTypeName)) {
                bdi.setMessageTypeName(m.get(messageTypeName));
            }
        });
    }

    void changeNounIfNeeded(HeaderDto headerDto) {
        String noun = headerDto.getNoun();
        if (!letscoProperties.getInputFile().getGenericNouns().keySet().stream()
                .map(BasicGenericNounEnum::getNoun).collect(toList()).contains(noun)) {
            for (Map.Entry<BasicGenericNounEnum, List<String>> e :
                    letscoProperties.getInputFile().getGenericNouns().entrySet()) {
                if (e.getValue().contains(noun)) {
                    headerDto.setNoun(e.getKey().getNoun());
                    break;
                }
            }
        }
    }

    void changeSourceIfNeeded(EventMessageDto eventMessageDto) {
        String source = eventMessageDto.getHeader().getSource();
        letscoProperties.getInputFile().getValidation().getChangeSource().ifPresent(m -> {
            if (m.containsKey(source)) {
                if (m.get(source).getFromWhichLevel() == ChangeJsonDataFromWhichEnum.HEADER) {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> headerMap = mapper.convertValue(eventMessageDto.getHeader(), Map.class);
                    String newSource = headerMap.get(m.get(source).getChangingField()).toString();
                    eventMessageDto.getHeader().setSource(newSource);
                } else if (m.get(source).getFromWhichLevel() == ChangeJsonDataFromWhichEnum.BUSINESS_DATA_IDENTIFIER) {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> bdiMap = mapper.convertValue(
                            eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier(), Map.class);
                    String newSource = bdiMap.get(m.get(source).getChangingField()).toString();
                    eventMessageDto.getHeader().setSource(newSource);
                }
            }
        });
    }

    boolean isGenericNoun(String noun) {
        return letscoProperties.getInputFile().allGenericNouns().contains(noun);
    }
}
