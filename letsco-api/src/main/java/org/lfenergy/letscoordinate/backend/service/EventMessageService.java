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

package org.lfenergy.letscoordinate.backend.service;

import io.vavr.control.Validation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.config.LetscoProperties;
import org.lfenergy.letscoordinate.backend.dto.ResponseErrorDto;
import org.lfenergy.letscoordinate.backend.dto.ResponseErrorMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.BusinessDataIdentifierDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.HeaderDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.*;
import org.lfenergy.letscoordinate.backend.enums.ResponseErrorSeverityEnum;
import org.lfenergy.letscoordinate.backend.enums.UnknownEicCodesProcessEnum;
import org.lfenergy.letscoordinate.backend.util.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventMessageService {

    private final CoordinationConfig coordinationConfig;
    private final LetscoProperties letscoProperties;

    public Validation<ResponseErrorDto, EventMessageDto> validateEventMessageDto(EventMessageDto eventMessageDto) {
        if (eventMessageDto == null) {
            return Validation.invalid(ResponseErrorDto.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .code("INVALID_INPUT_FILE")
                    .messages(Collections.singletonList(
                            ResponseErrorMessageDto.builder()
                                    .severity(ResponseErrorSeverityEnum.ERROR)
                                    .message("eventMessageDto can not be null!")
                                    .build()
                    ))
                    .build());
        }

        List<ResponseErrorMessageDto> errorMessages = new ArrayList<>();

        // check that all mandatory fields are provided
        if (eventMessageDto.getPayload() != null && eventMessageDto.getPayload().getText() == null
                && eventMessageDto.getPayload().getLinks() == null
                && eventMessageDto.getPayload().getRscKpi() == null
                && eventMessageDto.getPayload().getTimeserie() == null
                && eventMessageDto.getPayload().getValidation().isEmpty()
        ) {
            errorMessages.add(ResponseErrorMessageDto.builder()
                    .severity(ResponseErrorSeverityEnum.ERROR)
                    .message("The payload block should not be empty!")
                    .build());
        }
        Set<String> missingMandatoryFields = getMissingMandatoryFields(eventMessageDto);
        if(!missingMandatoryFields.isEmpty()) {
            errorMessages.add(ResponseErrorMessageDto.builder()
                    .severity(ResponseErrorSeverityEnum.ERROR)
                    .message("Missing mandatory values! >>> " + missingMandatoryFields.toString())
                    .build());
        }

        // check that all eic_code provided by the dto exists in our database
        checkEicCodes(eventMessageDto, errorMessages);

        if(!errorMessages.isEmpty()) {
            return Validation.invalid(ResponseErrorDto.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .code("INVALID_INPUT_FILE")
                    .messages(errorMessages)
                    .build());
        }
        return Validation.valid(eventMessageDto);
    }

    public Set<String> getMissingMandatoryFields(EventMessageDto eventMessageDto) {
        Set<String> missingMandatoryFields = new LinkedHashSet<>();
        if (eventMessageDto != null) {
            ValidatorFactory factory = javax.validation.Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<EventMessageDto>> violations = validator.validate(eventMessageDto);
            missingMandatoryFields = violations.stream()
                    .map(ConstraintViolation::getPropertyPath)
                    .map(Path::toString)
                    .collect(Collectors.toSet());
        }
        if (letscoProperties.getInputFile().getValidation().isBusinessDayFromOptional()) {
            if (missingMandatoryFields.contains("header.properties.businessDataIdentifier.businessDayFrom")) {
                if (eventMessageDto != null) {
                    eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier()
                            .setBusinessDayFrom(eventMessageDto.getHeader().getTimestamp());
                    missingMandatoryFields.remove("header.properties.businessDataIdentifier.businessDayFrom");
                }
            }
        }
        if (letscoProperties.getInputFile().getValidation().isValidationBusinessTimestampOptional()) {
            missingMandatoryFields = missingMandatoryFields.stream().filter(
                    f -> !f.matches("payload\\.validation\\.validationMessages\\[[0-9]+\\]\\.businessTimestamp"))
                    .collect(Collectors.toSet());
        }
        return missingMandatoryFields;
    }

    public void checkEicCodes(EventMessageDto eventMessageDto, List<ResponseErrorMessageDto> errorMessages) {
        // check that all eic_code provided by the dto exists in our database
        Set<String> dtoEicCodes = extractEicCodesFromEventMessageDto(eventMessageDto);
        List<String> knownEicCodes = getKnownEicCodes(dtoEicCodes);
        knownEicCodes.addAll(Arrays.asList(Constants.ALL_RSCS_CODE, Constants.ALL_REGIONS_CODE));
        Set<String> unknownEicCodes = dtoEicCodes.stream()
                .filter(eicCode -> !knownEicCodes.contains(eicCode))
                .collect(Collectors.toSet());
        if(!unknownEicCodes.isEmpty()) {
            UnknownEicCodesProcessEnum unknownEicCodesProcess =
                    letscoProperties.getInputFile().getValidation().getUnknownEicCodesProcess();
            if(unknownEicCodesProcess == UnknownEicCodesProcessEnum.EXCEPTION) {
                List<String> allowedEicCodes = letscoProperties.getInputFile().getValidation().getAllowedEicCodes()
                        .orElse(new ArrayList<>());
                if (!allowedEicCodes.containsAll(unknownEicCodes)) {
                    errorMessages.add(ResponseErrorMessageDto.builder()
                            .severity(ResponseErrorSeverityEnum.ERROR)
                            .message("Unknown eic_codes found! >>> " + unknownEicCodes.toString())
                            .build());
                }
            } else if (unknownEicCodesProcess == UnknownEicCodesProcessEnum.WARNING) {
                log.warn("Unknown eic_codes found! >>> " + unknownEicCodes.toString());
            }
        }
    }

    public List<String> getKnownEicCodes(Set<String> eicCodes) {
        return coordinationConfig.getAllEicCodes().stream()
                .filter(eicCode -> eicCodes.contains(eicCode))
                .collect(Collectors.toList());
    }

    private Set<String> extractEicCodesFromEventMessageDto(EventMessageDto eventMessageDto) {
        Set<String> eicCodes = new HashSet<>();
        if(eventMessageDto == null)
            return eicCodes;

        HeaderDto headerDto = eventMessageDto.getHeader();
        if(headerDto != null && headerDto.getProperties() != null && headerDto.getProperties().getBusinessDataIdentifier() != null) {
            BusinessDataIdentifierDto businessDataIdentifierDto = headerDto.getProperties().getBusinessDataIdentifier();
            businessDataIdentifierDto.getSendingUser().ifPresent(eicCodes::add);
            businessDataIdentifierDto.getTso().ifPresent(eicCodes::add);
            businessDataIdentifierDto.getBiddingZone().ifPresent(eicCodes::add);
        }

        PayloadDto payloadDto = eventMessageDto.getPayload();
        if(payloadDto != null) {
            if(payloadDto.getLinks() != null) {
                eicCodes.addAll(payloadDto.getLinks().stream()
                        .filter(Objects::nonNull)
                        .map(LinkDataDto::getEicCode)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList()));
            }
            if(payloadDto.getRscKpi() != null) {
                eicCodes.addAll(payloadDto.getRscKpi().stream()
                        .filter(Objects::nonNull)
                        .map(RscKpiDataDto::getData)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .filter(Objects::nonNull)
                        .map(RscKpiDataDetailsDto::getDetail)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .filter(Objects::nonNull)
                        .map(RscKpiTemporalDataDto::getEicCode)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList()));
            }
            if(payloadDto.getTimeserie() != null) {
                eicCodes.addAll(payloadDto.getTimeserie().stream()
                        .filter(Objects::nonNull)
                        .map(TimeserieDataDto::getData)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .filter(Objects::nonNull)
                        .map(TimeserieDataDetailsDto::getDetail)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .filter(Objects::nonNull)
                        .map(TimeserieTemporalDataDto::getEicCode)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList()));
            }
        }

        return eicCodes;
    }
}
