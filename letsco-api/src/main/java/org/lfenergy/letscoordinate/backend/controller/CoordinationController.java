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

package org.lfenergy.letscoordinate.backend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.vavr.control.Validation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.letscoordinate.backend.component.OpfabPublisherComponent;
import org.lfenergy.letscoordinate.backend.model.Coordination;
import org.lfenergy.letscoordinate.backend.model.CoordinationLttdQueue;
import org.lfenergy.letscoordinate.backend.model.EventMessage;
import org.lfenergy.letscoordinate.backend.service.CoordinationService;
import org.opfab.cards.model.Card;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static org.lfenergy.letscoordinate.backend.enums.FileDirectionEnum.OUTPUT;

@RestController
@RequestMapping("/letsco/api/v1")
@RequiredArgsConstructor
@Api(description = "Controller providing APIs to manage coordination", hidden = true)
@Slf4j
public class CoordinationController {

    private final CoordinationService coordinationService;
    private final OpfabPublisherComponent opfabPublisherComponent;

    @PostMapping("/coordination")
    @ApiOperation(value = "Coordination callback", hidden = true)
    @ResponseStatus(HttpStatus.OK)
    public void coordinationCallback(@RequestBody Card card) throws IOException {
        log.info("Callback card received:\n" + card.toString());
        Validation<Boolean, Coordination> validation = coordinationService.saveAnswersAndCheckIfAllTsosHaveAnswered(card);
        if (validation.isValid()) {
            if (validation.get().getLttd() == null) {
                log.info("All required entities have been answered! => Publishing result card... (coordinationId = {})", validation.get().getId());
                publishResultAndOutputFileCards(validation.get());
                log.info("Result card published with success! => (coordinationId = {})", validation.get().getId());
            } else {
                log.info("All entities have answered, but result card will be sent at LTTD = {} !", validation.get().getLttd().toString());
            }
        } else {
            log.info("Some entities did not respond yet!");
        }
    }

    @Scheduled(fixedDelayString = "#{@letscoProperties.coordination.lttd.schedulerFixedDelay}", zone = "#{@letscoProperties.timezone.id}")
    public void manageCardsLTTD() {
        try {
            List<CoordinationLttdQueue> lttdPassedCoordinations = coordinationService.getLttdExpiredCoordinations();
            log.debug("Number of coordinations with LTTD expired => {}", lttdPassedCoordinations.size());
            lttdPassedCoordinations.forEach(coordinationLttdQueue -> {
                try {
                    log.info("LTTD expired! => Publishing result card... (coordinationId = {})", coordinationLttdQueue.getCoordination().getId());
                    publishResultAndOutputFileCards(coordinationLttdQueue.getCoordination());
                    log.debug("Updating coordination status and removing its index from the LTTD queue...");
                    coordinationService.updateCoordinationProcessStatusAndRemoveItFromLttdQueue(coordinationLttdQueue);
                    log.info("Result card published with success after LTTD expiration! => (coordinationId = {})", coordinationLttdQueue.getCoordination().getId());
                } catch (Exception e) {
                    log.error("An error occurred while managing coordination with LTTD expired! (coordinationId = {}) ",
                            coordinationLttdQueue.getCoordination().getId(), e);
                }
            });
        } catch (Exception e) {
            log.error("An error occurred while managing coordinations LTTD waiting list! ", e);
        }
    }

    protected void publishResultAndOutputFileCards(Coordination coordination) throws IOException {
        log.debug("Applying coordination answers to eventmessage...");
        EventMessage updatedEventMessage = coordinationService.applyCoordinationAnswersToEventMessage(coordination);
        log.debug("Publishing coordination result card...");
        Card resultCard = opfabPublisherComponent.publishOpfabCoordinationResultCard(coordination);
        log.debug("Sending coordination output file to kafka...");
        boolean outputFileGenerated = coordinationService.sendOutputFileToKafka(updatedEventMessage);
        if (outputFileGenerated) {
            log.debug("Sending coordination output file card...");
            coordinationService.sendCoordinationFileCard(resultCard, OUTPUT);
        }
    }

}
