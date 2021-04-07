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

package org.lfenergy.letscoordinate.dataprovider.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.dataprovider.config.LetscoKafkaProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class KafkaProducerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    LetscoKafkaProperties letscoKafkaProperties;

    @BeforeEach
    public void before() {
        letscoKafkaProperties = new LetscoKafkaProperties();
    }

    @Test
    public void sendMessageToKafka_topicParamProvided() throws Exception {
        String data = "eventMessage as string";
        mockMvc.perform(post("/letsco/data-provider/v1/kafka/json/raw-msg")
                .param("topic", "topicName")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(data))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(data));
    }

    @Test
    public void sendMessageToKafka_topicParamNotProvided() throws Exception {
        letscoKafkaProperties.setTopicNamePrefix("topic_prefix_");
        String data = "eventMessage as string";
        mockMvc.perform(post("/letsco/data-provider/v1/kafka/json/raw-msg")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(data))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(data));
    }

}
