/*
 * Copyright (c) 2020, RTE (https://www.rte-france.com)
 * Copyright (c) 2020 RTE international (https://www.rte-international.com)
 * See AUTHORS.txt
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 * This file is part of the Let’s Coordinate project.
 */

package org.lfenergy.letscoordinate.backend.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.config.LetscoProperties;
import org.lfenergy.letscoordinate.backend.config.OpfabConfig;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.BusinessDataIdentifierDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.ValidationDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.ValidationMessageDto;
import org.lfenergy.letscoordinate.backend.enums.MessageTypeEnum;
import org.lfenergy.letscoordinate.backend.enums.ValidationSeverityEnum;
import org.lfenergy.letscoordinate.backend.model.opfab.ValidationData;
import org.lfenergy.letscoordinate.backend.util.*;
import org.lfenergy.operatorfabric.cards.model.*;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class OpfabPublisherComponent {

    private OpfabConfig opfabConfig;
    private Map<String, CoordinationConfig.Tso> tsos;
    private LetscoProperties letscoProperties;

    public OpfabPublisherComponent(OpfabConfig opfabConfig, CoordinationConfig coordinationConfig, LetscoProperties letscoProperties) {
        this.opfabConfig = opfabConfig;
        this.tsos = coordinationConfig.getTsos();
        this.letscoProperties = letscoProperties;
    }

    public void publishOpfabCard(EventMessageDto eventMessageDto, Long id) {
        Card card = generateOpfabCard(eventMessageDto, id);
        HttpUtil.post(opfabConfig.getUrl().getCardsPub(), card);
    }

    public Card generateOpfabCard(EventMessageDto eventMessageDto, Long id) {

        Card card = new Card();

        setCardHeadersAndTags(card, eventMessageDto, id);
        setOpfabCardDates(card, eventMessageDto);
        specificCardTreatment(card, eventMessageDto, id);
        setCardRecipients(card, eventMessageDto);

        log.info("Opfab card generated");

        return card;
    }

    String getValidationName(EventMessageDto eventMessageDto) {
        ValidationSeverityEnum result = eventMessageDto.getPayload().getValidation().getResult();

        switch (result) {
            case OK:
                return StringUtils.capitalize(MessageTypeEnum.POSITIVE_VALIDATION.getValue());
            case WARNING:
                return StringUtils.capitalize(MessageTypeEnum.POSITIVE_VALIDATION_WITH_WARNINGS.getValue());
            case ERROR:
                return StringUtils.capitalize(MessageTypeEnum.NEGATIVE_VALIDATION.getValue());
            default:
                return "";
        }
    }

    void setCardHeadersAndTags(Card card, EventMessageDto eventMessageDto, Long id) {

        String source = eventMessageDto.getHeader().getSource();
        String noun = eventMessageDto.getHeader().getNoun();
        String messageTypeName = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getMessageTypeName();
        String processKey = generateProcessKey(source, messageTypeName); // Used to identify notification's template for all messages
        String processKeyWithNoun = generateProcessKeyWithNoun(source, messageTypeName, noun); // Used as tag for validation messages
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByStateId(StringUtil.toLowercaseIdentifier(messageTypeName));
        Assert.notNull(messageTypeEnum, "Unknown message type \"" + messageTypeName + "\"!");

        card.setTags(Arrays.asList(
                StringUtil.toLowercaseIdentifier(source),
                StringUtil.toLowercaseIdentifier(messageTypeName),
                Constants.PROCESS_ID_PROCESSMONITORING.equals(messageTypeEnum.getProcessId()) ? processKey : processKeyWithNoun,
                Constants.PROCESS_ID_PROCESSMONITORING.equals(messageTypeEnum.getProcessId()) ? generateStateKey(source, messageTypeName) : generateStateKeyWithNoun(source, messageTypeName, noun))
                .stream().map(String::toLowerCase).collect(Collectors.toSet()).stream().collect(Collectors.toList()));
        card.setProcess(Constants.PROCESS_ID_PROCESSMONITORING.equals(messageTypeEnum.getProcessId()) ? processKey : processKeyWithNoun);
        card.setProcessInstanceId(processKey + "_" + id);
        card.setPublisher(opfabConfig.getPublisher());
        card.setProcessVersion("1");
        card.setState(StringUtil.toLowercaseIdentifier(messageTypeName));
    }

    public void setOpfabCardDates(Card card, EventMessageDto eventMessageDto) {

        Instant timestamp = eventMessageDto.getHeader().getTimestamp();
        BusinessDataIdentifierDto businessDataIdentifierDto = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        Instant businessDayFrom = businessDataIdentifierDto.getBusinessDayFrom();
        Instant businessDayTo = businessDataIdentifierDto.getBusinessDayTo().get();

        List<TimeSpan> timeSpans = new ArrayList<>();
        ValidationDto validation = eventMessageDto.getPayload().getValidation();
        if (validation != null && validation.getValidationMessages().isPresent() && !validation.getValidationMessages().get().isEmpty()) {
            // LC-208 MR-1: If the card contains validation errors and/or warnings, we display a bubble in the timeline
            // for each error and/or warning found. No bubble to display for the card’s arrival time in this case.
            timeSpans = validation.getValidationMessages().get().stream()
                    .map(validationMessage -> new TimeSpan().start(validationMessage.getBusinessTimestamp()))
                    .collect(Collectors.toList());
        } else {
            // LC-207 MR-1 & LC-208 MR-2: If the card does not contain any validation error or warning, we display only
            // one bubble for the card’s arrival time in feed
            timeSpans.add(new TimeSpan().start(timestamp));
        }

        card.setTimeSpans(timeSpans);
        card.setPublishDate(timestamp);
        // MR1: The feed card’s start date is the minimum between the card’s arrival date (timestamp) and the beginning
        // of the business period (businessDayFrom)
        card.setStartDate(businessDayFrom.isBefore(timestamp) ? businessDayFrom : timestamp);
        card.setEndDate(businessDayTo);
    }

    void setCardRecipients(Card card, EventMessageDto eventMessageDto) {

        String source = eventMessageDto.getHeader().getSource();
        String noun = eventMessageDto.getHeader().getNoun();
        BusinessDataIdentifierDto businessDataIdentifier = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        Optional<String> tso = businessDataIdentifier.getTso();
        Optional<String> sendingUser = businessDataIdentifier.getSendingUser();
        Optional<List<String>> recipient = businessDataIdentifier.getRecipients();
        String messageTypeName = businessDataIdentifier.getMessageTypeName();
        String processKey = generateProcessKey(source, messageTypeName);

        card.setRecipient(new Recipient().type(RecipientEnum.GROUP).identity(StringUtil.toLowercaseIdentifier(source)));

        Set<String> entityRecipientList = new HashSet<>();
        tso.ifPresent(entityRecipientList::add);
        if (opfabConfig.getEntityRecipients().containsKey(processKey)) {
            OpfabConfig.OpfabEntityRecipients entityRecipients = opfabConfig.getEntityRecipients().get(processKey);
            if (!entityRecipients.getNotAllowed().equals("sendingUser")) {
                sendingUser.ifPresent(entityRecipientList::add);
            }
            if (!entityRecipients.getNotAllowed().equals("recipient")) {
                recipient.ifPresent(entityRecipientList::addAll);
            }
            if (entityRecipients.isAddRscs()) {
                entityRecipientList.addAll(tsos.entrySet().stream()
                        .filter(t -> entityRecipientList.contains(t.getKey()))
                        .map(Map.Entry::getValue)
                        .map(CoordinationConfig.Tso::getRsc).collect(Collectors.toList()));
            }
        }else {
            sendingUser.ifPresent(entityRecipientList::add);
            recipient.ifPresent(entityRecipientList::addAll);
        }
        card.setEntityRecipients(new ArrayList<>(entityRecipientList));

    }

    void specificCardTreatment(Card opfabCard, EventMessageDto eventMessageDto, Long cardId) {

        BusinessDataIdentifierDto businessDataIdentifierDto = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        Instant businessDayFrom = businessDataIdentifierDto.getBusinessDayFrom();
        Optional<Instant> businessDayToOpt = businessDataIdentifierDto.getBusinessDayTo();
        String source = eventMessageDto.getHeader().getSource();
        String messageTypeName = businessDataIdentifierDto.getMessageTypeName();
        Optional<String> processStep = businessDataIdentifierDto.getProcessStep();
        Optional<String> timeframe = businessDataIdentifierDto.getTimeframe();
        Optional<Integer> timeframeNumber = businessDataIdentifierDto.getTimeframeNumber();

        // TODO This param should be equal true when dealing with smart notifications
        opfabCard.setKeepChildCards(false);

        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByStateId(StringUtil.toLowercaseIdentifier(messageTypeName));
        if (messageTypeEnum != null) {
            switch (messageTypeEnum) {
                case PROCESS_SUCCESSFUL:
                case PROCESS_FAILED:
                    opfabCard.setSeverity(messageTypeEnum.getSeverity());
                    opfabCard.setData(generateCardData(eventMessageDto, cardId));
                    break;
                case POSITIVE_VALIDATION:
                case POSITIVE_VALIDATION_WITH_WARNINGS:
                case NEGATIVE_VALIDATION:
                    messageValidatedTreatment(opfabCard, eventMessageDto);
                    break;
                default:
                    opfabCard.setData(generateCardData(eventMessageDto, cardId));
                    opfabCard.setSeverity(SeverityEnum.INFORMATION);
            }

            String title = generateFeedTitle(messageTypeEnum, source, processStep, eventMessageDto);
            Map<String, String> params = new HashMap<>();
            params.put("title", title);
            opfabCard.setTitle(new I18n().key("cardFeed.title").parameters(params));

            String summary = generateFeedSummary(messageTypeEnum, timeframe, timeframeNumber, businessDayFrom, businessDayToOpt, eventMessageDto);
            Map<String, String> summaryParams = new HashMap<>();
            summaryParams.put("summary", summary);
            opfabCard.setSummary(new I18n().key("cardFeed.summary").parameters(summaryParams));
        }
    }

    private String generateFeedTitle(MessageTypeEnum messageTypeEnum, String source, Optional<String> processStep, EventMessageDto eventMessageDto) {
        String key = Constants.PROCESS_ID_PROCESSMONITORING.equals(messageTypeEnum.getProcessId())
                ? generateStateKey(eventMessageDto)
                : generateStateKeyWithNoun(eventMessageDto);
        if (opfabConfig.getFeed().containsKey(key)) {
            String title = opfabConfig.getFeed().get(key).getTitle();
            title = replacePlaceholders(title, eventMessageDto);
            return title;
        } else {
            return String.format("%s %s%s", source, messageTypeEnum.getValue(), processStep.map(p -> " - " + p).orElse(""));
        }
    }

    private String generateFeedSummary(MessageTypeEnum messageTypeEnum, Optional<String> timeframe,
                                       Optional<Integer> timeframeNumber, Instant businessDayFrom,
                                       Optional<Instant> businessDayToOpt, EventMessageDto eventMessageDto) {
        String key = Constants.PROCESS_ID_PROCESSMONITORING.equals(messageTypeEnum.getProcessId())
                ? generateStateKey(eventMessageDto)
                : generateStateKeyWithNoun(eventMessageDto);
        if (opfabConfig.getFeed().containsKey(key)) {
            String summary = opfabConfig.getFeed().get(key).getSummary();
            summary = replacePlaceholders(summary, eventMessageDto);
            return summary;
        } else {
            return String.format("%s%s%s%s", timeframe.orElse(""), timeframeNumber.map(tn -> tn + " ").orElse(""),
                    DateUtil.formatDate(businessDayFrom.atZone(letscoProperties.getTimezone())),
                    businessDayToOpt
                            .map(instant -> "-" + DateUtil.formatDate(instant.atZone(letscoProperties.getTimezone())))
                            .orElse(""));
        }
    }

    String replacePlaceholders(String title, EventMessageDto eventMessageDto) {

        // If the title doesn't contain placeholders
        if (!Pattern.compile("\\{\\{.*\\}\\}").matcher(title).find()) {
            return title;

        } else {

            BusinessDataIdentifierDto businessDataIdentifierDto =
                    eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
            ObjectMapper mapper = new ObjectMapper().registerModule(
                    new SimpleModule().addSerializer(Instant.class, new JacksonUtil.InstantSerializer()));
            Map<String, Object> bdiMap = mapper.convertValue(businessDataIdentifierDto, Map.class);
            bdiMap.put("noun", eventMessageDto.getHeader().getNoun());

            Map<String, String> allMatches = new HashMap<>();
            Matcher m = Pattern.compile("\\{\\{.*?\\}\\}").matcher(title);
            while (m.find()) {
                allMatches.put(m.group(), null);
            }
            allMatches = allMatches.entrySet().stream().map(e -> generatePlaceholderValue(e, bdiMap,eventMessageDto))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            for (Map.Entry<String, String> entry : allMatches.entrySet())
                title = title.replace(entry.getKey(), entry.getValue());

            return title;
        }
    }

    Map.Entry<String, String> generatePlaceholderValue(Map.Entry<String, String> entry, Map<String, Object> bdiMap, EventMessageDto eventMessageDto) {

        String placeholder = entry.getKey();
        String placeholderValue = null;

        // Examples:
        //      ${businessDayFrom::dateFormat(HH:mm)} becomes businessDayFrom::dateFormat(HH:mm)
        //      ${businessDayFrom} becomes businessDayFrom
        String placeholderNoDelimiters = placeholder.replaceAll("(\\{\\{|::.*|\\}\\})", "");

        // If a format method is specified
        if (placeholder.contains("::")) {
            String formatMethod = placeholder.replaceAll(".*::|\\(.*\\)\\}\\}", "");
            if ("dateFormat".equals(formatMethod)) {
                String dateFormat =
                        placeholder.replaceAll(".*" + formatMethod + "\\(|\\)\\}\\}", "");
                placeholderValue =
                        DateTimeFormatter.ofPattern(dateFormat).format(
                                Instant.parse(bdiMap.get(placeholderNoDelimiters).toString())
                                        .atZone(letscoProperties.getTimezone()));
                return new AbstractMap.SimpleEntry(placeholder, placeholderValue);
            } if ("eicToName".equals(formatMethod)) {
                placeholderValue = tsos.get(bdiMap.get(placeholderNoDelimiters)).getName();
                return new AbstractMap.SimpleEntry(placeholder, placeholderValue);
            } else if ("notificationTitle".equals(formatMethod)) {
                placeholderValue = getValidationName(eventMessageDto);
                return new AbstractMap.SimpleEntry(placeholder, placeholderValue);
            } else {
                log.error("The placeholder method " + formatMethod + " is not valid!");
                placeholderValue = bdiMap.get(placeholderNoDelimiters) != null ?
                        bdiMap.get(placeholderNoDelimiters).toString() : "";
            }
        } else {
            placeholderValue = bdiMap.get(placeholderNoDelimiters) != null ?
                    bdiMap.get(placeholderNoDelimiters).toString() : "";
        }
        return new AbstractMap.SimpleEntry(placeholder, placeholderValue);
    }

    private void messageValidatedTreatment(Card card, EventMessageDto eventMessageDto) {
        Optional<List<ValidationMessageDto>> validationMessagesOpt = eventMessageDto.getPayload().getValidation().getValidationMessages();
        ValidationSeverityEnum result = eventMessageDto.getPayload().getValidation().getResult();

        switch (result) {
            case OK:
                card.setSeverity(SeverityEnum.COMPLIANT);
                break;
            case WARNING:
                card.setSeverity(SeverityEnum.ACTION);
                break;
            case ERROR:
                card.setSeverity(SeverityEnum.ALARM);
                break;
        }

        ValidationData data = new ValidationData(eventMessageDto);
        validationMessagesOpt.ifPresent(validationMessages -> {
            List<ValidationMessageDto> warnings = validationMessages.stream()
                    .filter(m -> m.getSeverity() == ValidationSeverityEnum.WARNING).collect(Collectors.toList());
            List<ValidationMessageDto> errors = validationMessages.stream()
                    .filter(m -> m.getSeverity() == ValidationSeverityEnum.ERROR).collect(Collectors.toList());
            data.setWarnings(warnings);
            data.setErrors(errors);
        });
        if (eventMessageDto != null && eventMessageDto.getHeader() != null && eventMessageDto.getHeader().getProperties() != null
                && eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier() != null
                && eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getSendingUser().isPresent()) {
            String sendingUserEicCode = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getSendingUser().get();
            if (tsos.get(sendingUserEicCode) != null)
                data.setSendingUser(tsos.get(sendingUserEicCode).getName());
        }
        card.setData(data);
    }

    Map<String, Object> generateCardData(EventMessageDto eventMessageDto, Long cardId) {
        BusinessDataIdentifierDto businessDataIdentifier =
                eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        Map<String, Object> data = new HashMap<>();
        data.put("cardId", cardId);
        data.put("noun", eventMessageDto.getHeader().getNoun());
        data.put("source", eventMessageDto.getHeader().getSource());
        data.put("timestamp", eventMessageDto.getHeader().getTimestamp());
        data.put("businessDataIdentifier", businessDataIdentifier);
        businessDataIdentifier.getSendingUser().ifPresent(u -> {
            if (tsos.containsKey(u))
                data.put("sendingUser", tsos.get(u).getName());
        });
        businessDataIdentifier.getRecipients().ifPresent(rl -> {
            List<String> recipentTsos = rl.stream().filter(r -> tsos.containsKey(r))
                    .map(r -> tsos.get(r).getName()).collect(Collectors.toList());
            if (!recipentTsos.isEmpty())
                data.put("recipient", recipentTsos);
        });
        data.put("payload", eventMessageDto.getPayload());
        return data;
    }

    private String generateProcessKey(EventMessageDto eventMessageDto) {
        String source = eventMessageDto.getHeader().getSource();
        String messageTypeName = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getMessageTypeName();
        return generateProcessKey(source, messageTypeName);
    }

    private String generateProcessKey(String source, String messageTypeName) {
        return new StringBuilder().append(StringUtil.toLowercaseIdentifier(source))
                .append("_")
                .append(MessageTypeEnum.getByStateId(StringUtil.toLowercaseIdentifier(messageTypeName)).getProcessId())
                .toString();
    }

    private String generateProcessKeyWithNoun(String source, String messageTypeName, String noun) {
        return new StringBuilder().append(StringUtil.toLowercaseIdentifier(source))
                .append("_")
                .append(StringUtil.toLowercaseIdentifier(noun))
                .append("_")
                .append(MessageTypeEnum.getByStateId(StringUtil.toLowercaseIdentifier(messageTypeName)).getProcessId())
                .toString();
    }

    private String generateStateKey(EventMessageDto eventMessageDto) {
        String source = eventMessageDto.getHeader().getSource();
        String messageTypeName = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getMessageTypeName();
        return generateStateKey(source, messageTypeName);
    }

    private String generateStateKeyWithNoun(EventMessageDto eventMessageDto) {
        String source = eventMessageDto.getHeader().getSource();
        String noun = eventMessageDto.getHeader().getNoun();
        String messageTypeName = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getMessageTypeName();
        return generateStateKeyWithNoun(source, messageTypeName, noun);
    }

    private String generateStateKey(String source, String messageTypeName) {
        return new StringBuilder().append(generateProcessKey(source, messageTypeName))
                .append("_")
                .append(StringUtil.toLowercaseIdentifier(messageTypeName))
                .toString();
    }

    private String generateStateKeyWithNoun(String source, String messageTypeName, String noun) {
        return new StringBuilder().append(generateProcessKeyWithNoun(source, messageTypeName, noun))
                .append("_")
                .append(StringUtil.toLowercaseIdentifier(messageTypeName))
                .toString();
    }

}