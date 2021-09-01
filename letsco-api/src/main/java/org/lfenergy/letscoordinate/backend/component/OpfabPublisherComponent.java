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
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.config.LetscoProperties;
import org.lfenergy.letscoordinate.backend.config.OpfabConfig;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.BusinessDataIdentifierDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.PayloadDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.ValidationDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.ValidationMessageDto;
import org.lfenergy.letscoordinate.backend.enums.ValidationSeverityEnum;
import org.lfenergy.letscoordinate.backend.model.opfab.ValidationData;
import org.lfenergy.letscoordinate.backend.util.*;
import org.lfenergy.operatorfabric.cards.model.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lfenergy.letscoordinate.backend.config.OpfabConfig.ChangeTimeserieDataDetailValueTypeEnum.INSTANT;
import static org.lfenergy.letscoordinate.backend.util.Constants.*;

@Component
@Slf4j
public class OpfabPublisherComponent {

    private String processKey;
    private OpfabConfig opfabConfig;
    private Map<String, CoordinationConfig.Tso> tsos;
    private Map<String, CoordinationConfig.Rsc> rscs;
    private LetscoProperties letscoProperties;

    public OpfabPublisherComponent(OpfabConfig opfabConfig, CoordinationConfig coordinationConfig, LetscoProperties letscoProperties) {
        this.opfabConfig = opfabConfig;
        this.tsos = coordinationConfig.getTsos();
        this.rscs = coordinationConfig.getRscs();
        this.letscoProperties = letscoProperties;
    }

    void setProcessKey(String processKey) {
        this.processKey = processKey;
    }

    public void publishOpfabCard(EventMessageDto eventMessageDto, Long id) {
        processKey = OpfabUtil.generateProcessKey(eventMessageDto);
        List<Card> cards = generateOpfabCards(eventMessageDto, id);
        cards.forEach(c -> {
            // TODO This field must be handled in future releases for response cards
            c.setKeepChildCards(false);
            c.setPublisherType(PublisherTypeEnum.EXTERNAL);
            HttpUtil.post(opfabConfig.getUrl().getCardsPub(), c);
        });
    }

    public List<Card> generateOpfabCards(EventMessageDto eventMessageDto, Long id) {

        List<Card> cards = new ArrayList<>();

        if (!opfabConfig.getSeparateCardsForRecipients().isEmpty() &&
                opfabConfig.getSeparateCardsForRecipients().contains(processKey)) {
            List<String> recipients = new ArrayList<>(eventMessageDto.getHeader().getProperties()
                    .getBusinessDataIdentifier().getRecipients().orElse(new ArrayList<>()));
            recipients.forEach(recipient -> {
                eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setRecipients(List.of(recipient));
                cards.add(generateOpfabCard(eventMessageDto, id));
            });
        } else {
            cards.add(generateOpfabCard(eventMessageDto, id));
        }
        return cards;
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
        ValidationSeverityEnum result = eventMessageDto.getPayload().getValidation().get().getResult();
        switch (result) {
            case OK:
                return POSITIVE_ACK;
            case WARNING:
                return POSITIVE_ACK_WITH_WARNINGS;
            case ERROR:
                return NEGATIVE_ACK;
            default:
                return "";
        }
    }

    void setCardHeadersAndTags(Card card, EventMessageDto eventMessageDto, Long id) {
        String noun = eventMessageDto.getHeader().getNoun();
        String source = eventMessageDto.getHeader().getSource();
        BusinessDataIdentifierDto bdi = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        String messageTypeName = bdi.getMessageTypeName();
        List<String> tags = Stream.of(source, messageTypeName, processKey, source + "_" + noun)
                .map(StringUtil::toLowercaseIdentifier).distinct().collect(Collectors.toList());
        if (MESSAGE_VALIDATED.equals(noun)) {
            Optional<String> filenameOpt = bdi.getFileName();
            ValidationSeverityEnum result = eventMessageDto.getPayload().getValidation().get().getResult();
            tags.add((processKey + "_" + result).toLowerCase());
            filenameOpt.ifPresent(f -> tags.add((processKey + "_" + f + result).toLowerCase()));
            fillQualityCheckSpecificTag(processKey, result, tags);
            card.setState(result.toString().toLowerCase());
        } else {
            opfabConfig.getTags().ifPresent(t -> {
                if (t.containsKey(processKey)) {
                    tags.add(t.get(processKey).getTag());
                }
            });
            setOpfabCardState(card, messageTypeName);
        }
        card.setTags(tags);
        setOpfabCardProcess(card);
        card.setProcessInstanceId(processKey + "_" + id);
        card.setPublisher(opfabConfig.getPublisher());
        card.setProcessVersion("1");
    }

    private void setOpfabCardProcess(Card card) {
        if (opfabConfig.getChangeProcess().containsKey(processKey)) {
            card.setProcess(opfabConfig.getChangeProcess().get(processKey));
        } else {
            card.setProcess(processKey);
        }
    }

    private void setOpfabCardState(Card card, String messageTypeName) {
        if (opfabConfig.getChangeState().containsKey(processKey)) {
            card.setState(opfabConfig.getChangeState().get(processKey));
        } else {
            card.setState(StringUtil.toLowercaseIdentifier(messageTypeName));
        }
    }

    private void fillQualityCheckSpecificTag(String process, ValidationSeverityEnum result, List<String> tags) {
        opfabConfig.getTags().ifPresent(t -> {
            if (t.containsKey(process)) {
                String tagSuffix = "";
                if (result == ValidationSeverityEnum.OK) {
                    if (t.get(process).getQcTagOk().isPresent()) {
                        tagSuffix = t.get(process).getQcTagOk().get();
                    }
                }
                if (result == ValidationSeverityEnum.WARNING) {
                    if (t.get(process).getQcTagWarning().isPresent()) {
                        tagSuffix = t.get(process).getQcTagWarning().get();
                    }
                }
                if (result == ValidationSeverityEnum.ERROR) {
                    if (t.get(process).getQcTagError().isPresent()) {
                        tagSuffix = t.get(process).getQcTagError().get();
                    }
                }
                if ("".equals(tagSuffix)) {
                    tagSuffix = result.toString().toLowerCase();
                }
                tags.add(t.get(process).getTag() + "_" + tagSuffix);
            }
        });
    }

    public void setOpfabCardDates(Card card, EventMessageDto eventMessageDto) {

        Instant timestamp = eventMessageDto.getHeader().getTimestamp();
        BusinessDataIdentifierDto businessDataIdentifierDto =
                eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        Instant businessDayFrom = businessDataIdentifierDto.getBusinessDayFrom();
        Instant businessDayTo = businessDataIdentifierDto.getBusinessDayTo();

        List<TimeSpan> timeSpans = new ArrayList<>();
        Optional<ValidationDto> validationOpt = eventMessageDto.getPayload().getValidation();
        if (validationOpt.isPresent() && validationOpt.get().getValidationMessages().isPresent() &&
                !validationOpt.get().getValidationMessages().get().isEmpty()) {
            timeSpans = validationOpt.get().getValidationMessages().get().stream()
                    .filter(validationMessage -> validationMessage.getBusinessTimestamp() != null)
                    .map(validationMessage -> new TimeSpan().start(validationMessage.getBusinessTimestamp()))
                    .collect(Collectors.toList());
        } else {
            timeSpans.add(new TimeSpan().start(timestamp));
        }

        card.setTimeSpans(timeSpans);
        card.setPublishDate(timestamp);
        card.setStartDate(businessDayFrom.isBefore(timestamp) ? businessDayFrom : timestamp);
        card.setEndDate(businessDayTo);
    }

    void setCardRecipients(Card card, EventMessageDto eventMessageDto) {

        String source = eventMessageDto.getHeader().getSource();
        BusinessDataIdentifierDto businessDataIdentifier = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        Optional<String> tso = businessDataIdentifier.getTso();
        Optional<String> sendingUser = businessDataIdentifier.getSendingUser();
        Optional<List<String>> recipient = businessDataIdentifier.getRecipients();

        card.setRecipient(new Recipient().type(RecipientEnum.GROUP).identity(StringUtil.toLowercaseIdentifier(source)));

        Set<String> entityRecipientList = new HashSet<>();
        tso.ifPresent(entityRecipientList::add);
        if (opfabConfig.getEntityRecipients().containsKey(processKey)) {
            OpfabConfig.OpfabEntityRecipients entityRecipients = opfabConfig.getEntityRecipients().get(processKey);
            String notAllowed = entityRecipients.getNotAllowed().orElse("");
            if (!"sendingUser".equals(notAllowed)) {
                sendingUser.ifPresent(entityRecipientList::add);
            }
            if (!"recipient".equals(notAllowed)) {
                recipient.ifPresent(entityRecipientList::addAll);
            }
            if (entityRecipients.isAddRscs()) {
                entityRecipientList.addAll(tsos.entrySet().stream()
                        .filter(t -> entityRecipientList.contains(t.getKey()))
                        .map(Map.Entry::getValue)
                        .map(CoordinationConfig.Tso::getRsc).collect(Collectors.toList()));
            }
        } else {
            sendingUser.ifPresent(entityRecipientList::add);
            recipient.ifPresent(entityRecipientList::addAll);
        }
        card.setEntityRecipients(new ArrayList<>(entityRecipientList));
    }

    void specificCardTreatment(Card opfabCard, EventMessageDto eventMessageDto, Long cardId) {

        BusinessDataIdentifierDto businessDataIdentifierDto =
                eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        String noun = eventMessageDto.getHeader().getNoun();
        Instant businessDayFrom = businessDataIdentifierDto.getBusinessDayFrom();
        Instant businessDayTo = businessDataIdentifierDto.getBusinessDayTo();
        Optional<String> processStepOpt = businessDataIdentifierDto.getProcessStep();
        Optional<String> timeframeOpt = businessDataIdentifierDto.getTimeframe();
        Optional<Integer> timeframeNumberOpt = businessDataIdentifierDto.getTimeframeNumber();

        String titleProcessType = "";
        switch (noun) {
            case PROCESS_SUCCESSFUL:
                titleProcessType = "process success";
                opfabCard.setSeverity(SeverityEnum.INFORMATION);
                opfabCard.setData(generateCardData(eventMessageDto, cardId));
                break;
            case PROCESS_FAILED:
                titleProcessType = "process failed";
                opfabCard.setSeverity(SeverityEnum.ALARM);
                opfabCard.setData(generateCardData(eventMessageDto, cardId));
                break;
            case MESSAGE_VALIDATED:
                titleProcessType = "message validated";
                messageValidatedTreatment(opfabCard, eventMessageDto);
                break;
            case PROCESS_ACTION:
                titleProcessType = "process action";
                opfabCard.setSeverity(SeverityEnum.ACTION);
                opfabCard.setData(generateCardData(eventMessageDto, cardId));
                break;
            case PROCESS_INFORMATION:
                titleProcessType = "process information";
                opfabCard.setSeverity(SeverityEnum.INFORMATION);
                opfabCard.setData(generateCardData(eventMessageDto, cardId));
                break;
            default:
                opfabCard.setData(generateCardData(eventMessageDto, cardId));
                opfabCard.setSeverity(SeverityEnum.INFORMATION);
        }

        String title = generateFeedTitle(titleProcessType, processStepOpt, eventMessageDto);
        Map<String, String> params = new HashMap<>();
        params.put("title", title);
        opfabCard.setTitle(new I18n().key("cardFeed.title").parameters(params));

        String summary = generateFeedSummary(timeframeOpt, timeframeNumberOpt, businessDayFrom, businessDayTo, eventMessageDto);
        Map<String, String> summaryParams = new HashMap<>();
        summaryParams.put("summary", summary);
        opfabCard.setSummary(new I18n().key("cardFeed.summary").parameters(summaryParams));
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
        businessDataIdentifier.getTso().ifPresent(u -> {
            if (tsos.containsKey(u))
                data.put("tso", tsos.get(u).getName());
        });
        businessDataIdentifier.getRecipients().ifPresent(rl -> {
            List<String> recipents = rl.stream().filter(r -> tsos.containsKey(r) || rscs.containsKey(r))
                    .map(r -> tsos.containsKey(r) ? tsos.get(r).getName() : rscs.get(r).getName())
                    .collect(Collectors.toList());
            if (!recipents.isEmpty())
                data.put("recipient", recipents);
        });
        addPayloadData(data, eventMessageDto.getPayload());
        return data;
    }

    private void addPayloadData(Map<String, Object> data, PayloadDto payloadDto) {
        if (opfabConfig.getData().containsKey(processKey)) {
            opfabConfig.getData().get(processKey).getChangeTimeserieDataDetailValueType().ifPresent(c ->
                    payloadDto.getTimeserie().forEach(t ->
                            t.getData().forEach(d ->
                                    d.getDetail().forEach(detail -> {
                                        if (c.containsKey(detail.getLabel())) {
                                            OpfabConfig.ChangeTimeserieDataDetailValueTypeEnum changeTimeserieDataDetailValueTypeEnum =
                                                    c.get(detail.getLabel());
                                            if (changeTimeserieDataDetailValueTypeEnum == INSTANT) {
                                                detail.setOpfabDataValue(Instant.parse(detail.getValue()));
                                            }
                                        }
                                    })
                            )
                    )
            );
        }
        data.put("payload", payloadDto);
    }

    private void messageValidatedTreatment(Card card, EventMessageDto eventMessageDto) {

        Optional<List<ValidationMessageDto>> validationMessagesOpt = eventMessageDto.getPayload().getValidation().get().getValidationMessages();
        ValidationSeverityEnum result = eventMessageDto.getPayload().getValidation().get().getResult();

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
        data.getPayload().getValidation().get().getValidationMessages().orElse(new ArrayList<>())
                .forEach(v -> v.setMessage(fillValidationParams(v)));
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getSendingUser()
                .ifPresent(u -> {
                    if (tsos.containsKey(u))
                        data.setSendingUser(tsos.get(u).getName());
                });
        validationMessagesOpt.ifPresent(validationMessages -> {
            List<ValidationMessageDto> warnings = validationMessages.stream()
                    .filter(m -> m.getSeverity() == ValidationSeverityEnum.WARNING).collect(Collectors.toList());
            List<ValidationMessageDto> errors = validationMessages.stream()
                    .filter(m -> m.getSeverity() == ValidationSeverityEnum.ERROR).collect(Collectors.toList());
            data.setWarnings(warnings);
            data.setErrors(errors);
        });
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getSendingUser().ifPresent(sendingUserEicCode -> {
            if (tsos.containsKey(sendingUserEicCode)) {
                data.setSendingUser(tsos.get(sendingUserEicCode).getName());
            } else if (rscs.get(sendingUserEicCode) != null) {
                data.setSendingUser(rscs.get(sendingUserEicCode).getName());
            }
        });
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getTso().ifPresent(tsoEicCode -> {
            if (tsos.containsKey(tsoEicCode)) {
                data.setTso(tsos.get(tsoEicCode).getName());
            }
        });
        card.setData(data);
    }

    private static final String PARAM_START_IDENTIFIER = "<";
    private static final String PARAM_END_IDENTIFIER = ">";

    /**
     * Fills placeholders in validation message from validation paramMap.
     *
     * @param validationResult validation message object
     * @return properly filled message without validation placeholders to be displayed on GUI
     */
    private String fillValidationParams(ValidationMessageDto validationResult) {
        Optional<Map<String, Object>> paramMapOpt = validationResult.getParams();
        String message = validationResult.getMessage();
        if (paramMapOpt.isPresent()) {
            for (Map.Entry<String, Object> param : paramMapOpt.get().entrySet()) {
                String paramKey = param.getKey();
                Object paramValue = paramMapOpt.get().get(paramKey);
                message = message.replace(PARAM_START_IDENTIFIER + paramKey + PARAM_END_IDENTIFIER, paramValue != null ? paramValue.toString() : "null");
            }
        }
        return message;
    }

    private String generateFeedTitle(String titleProcessType, Optional<String> processStep, EventMessageDto eventMessageDto) {
        if (opfabConfig.getFeed().containsKey(processKey)) {
            String title = opfabConfig.getFeed().get(processKey).getTitle();
            title = replacePlaceholders(title, eventMessageDto);
            return title;
        } else {
            return String.format("%s %s%s", eventMessageDto.getHeader().getSource(), titleProcessType, processStep.map(p -> " - " + p).orElse(""));
        }
    }

    private String generateFeedSummary(Optional<String> timeframe, Optional<Integer> timeframeNumber, Instant businessDayFrom,
                                       Instant businessDayTo, EventMessageDto eventMessageDto) {
        if (opfabConfig.getFeed().containsKey(processKey)) {
            String summary = opfabConfig.getFeed().get(processKey).getSummary();
            summary = replacePlaceholders(summary, eventMessageDto);
            return summary;
        } else {
            return String.format("%s%s%s%s", timeframe.orElse(""), timeframeNumber.map(tn -> tn + " ").orElse(""),
                    DateUtil.formatDate(businessDayFrom.atZone(letscoProperties.getTimezone())),
                    DateUtil.formatDate(businessDayTo.atZone(letscoProperties.getTimezone())));
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

            Map<String, String> allMatches = new HashMap<>();
            Matcher m = Pattern.compile("\\{\\{.*?\\}\\}").matcher(title);
            while (m.find()) {
                allMatches.put(m.group(), null);
            }
            allMatches = allMatches.entrySet().stream().map(e -> generatePlaceholderValue(e, bdiMap, eventMessageDto))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            for (Map.Entry<String, String> entry : allMatches.entrySet())
                title = title.replace(entry.getKey(), entry.getValue());

            return title;
        }
    }

    Map.Entry<String, String> generatePlaceholderValue
            (Map.Entry<String, String> entry, Map<String, Object> bdiMap,
             EventMessageDto eventMessageDto) {

        String placeholder = entry.getKey();
        String placeholderValue = null;

        // Examples:
        //      {{businessDayFrom::dateFormat(HH:mm)}} becomes businessDayFrom
        //      {{businessDayFrom}} becomes businessDayFrom
        String placeholderNoDelimiters = placeholder.replaceAll("(\\{\\{|::.*|\\}\\})", "");
        Optional<Object> bdiField = Optional.ofNullable(bdiMap.get(placeholderNoDelimiters));

        // If a format method is specified
        if (placeholder.contains("::")) {
            if (bdiField.isPresent()) {
                String formatMethod = placeholder
                        .replaceFirst("(.*?)::", "")
                        .replaceFirst("\\(.*\\)\\}\\}", "");
                if ("dateFormat".equals(formatMethod)) {
                    String dateFormatAndZoneId =
                            placeholder.replaceAll(".*" + formatMethod + "\\(|\\)\\}\\}", "");
                    String dateFormat = null, zoneId = null;
                    if (dateFormatAndZoneId.contains("::")) {
                        String[] dateFormatAndZoneIdFields = dateFormatAndZoneId.split("::");
                        dateFormat = dateFormatAndZoneIdFields[0];
                        zoneId = dateFormatAndZoneIdFields[1];
                    } else {
                        dateFormat = dateFormatAndZoneId;
                        zoneId = "Europe/Paris";
                    }
                    placeholderValue =
                            DateTimeFormatter.ofPattern(dateFormat).format(
                                    Instant.parse(bdiMap.get(placeholderNoDelimiters).toString())
                                            .atZone(ZoneId.of(zoneId)));
                    return new AbstractMap.SimpleEntry(placeholder, placeholderValue);
                } else if ("eicToName".equals(formatMethod)) {
                    if ("recipients".equals(placeholderNoDelimiters)) {
                        List<String> recipients = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier()
                                .getRecipients().orElse(new ArrayList<>());
                        placeholderValue = recipients.stream().map(eicCode -> {
                            if (tsos.get(eicCode) != null) {
                                return tsos.get(eicCode).getName();
                            } else if (rscs.get(eicCode) != null) {
                                return rscs.get(eicCode).getName();
                            } else {
                                return eicCode;
                            }
                        }).collect(Collectors.joining("/"));
                    } else {
                        String eicCode = bdiMap.get(placeholderNoDelimiters).toString();
                        if (tsos.get(eicCode) != null) {
                            placeholderValue = tsos.get(eicCode).getName();
                        } else if (rscs.get(eicCode) != null) {
                            placeholderValue = rscs.get(eicCode).getName();
                        } else {
                            placeholderValue = eicCode;
                        }
                    }
                    return new AbstractMap.SimpleEntry(placeholder, placeholderValue);
                } else {
                    log.error("The placeholder method " + formatMethod + "is not valid!");
                    placeholderValue = bdiMap.get(placeholderNoDelimiters) != null ?
                            bdiMap.get(placeholderNoDelimiters).toString() : "";
                }
            } else {
                return new AbstractMap.SimpleEntry(placeholder, "");
            }
        } else {
            if ("validationStatus".equals(placeholderNoDelimiters)) {
                placeholderValue = getValidationName(eventMessageDto);
                return new AbstractMap.SimpleEntry(placeholder, placeholderValue);
            } else {
                placeholderValue = bdiMap.get(placeholderNoDelimiters) != null ?
                        bdiMap.get(placeholderNoDelimiters).toString() : "";
            }
        }
        return new AbstractMap.SimpleEntry(placeholder, placeholderValue);
    }
}