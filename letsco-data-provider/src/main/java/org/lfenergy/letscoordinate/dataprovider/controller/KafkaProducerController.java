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

package org.lfenergy.letscoordinate.dataprovider.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.lfenergy.letscoordinate.dataprovider.config.LetscoKafkaProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping(value = "/letsco/data-provider/v1/kafka")
@RequiredArgsConstructor
public class KafkaProducerController {

    private final LetscoKafkaProperties letscoKafkaProperties;

    private void sendMessageToKafka(String topic, String data) {

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, letscoKafkaProperties.getBootstrapServers());
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer(properties);

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, data);
        producer.send(record);

        producer.close();
    }

    @PostMapping(value = "/json/raw-msg")
    public ResponseEntity sendRawMessage(@RequestBody String data, @RequestParam(required = false) String topic) {
        sendMessageToKafka(topic == null ? letscoKafkaProperties.getTopicNamePrefix() + "raw" : topic, data);
        return ResponseEntity.ok(data);
    }

}
