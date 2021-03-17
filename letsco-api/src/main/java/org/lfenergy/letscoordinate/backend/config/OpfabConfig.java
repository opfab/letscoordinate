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

package org.lfenergy.letscoordinate.backend.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

@ConfigurationProperties(prefix = "opfab")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpfabConfig {

    private String publisher;
    private OpfabUrls url;
    @Getter(AccessLevel.NONE)
    private Map<String, OpfabTagsConf> tags;
    private Map<String, OpfabFeed> feed = new HashMap<>();
    private Map<String, OpfabEntityRecipients> entityRecipients = new HashMap<>();
    private Map<String, ChangeTimeserieDataDetailValueType> data = new HashMap<>();
    private Map<String, String> changeProcess = new HashMap<>();
    private Map<String, String> changeState = new HashMap<>();
    private List<String> separateCardsForRecipients = new ArrayList<>();

    @Getter
    @Setter
    public static class OpfabUrls {
        private String cardsPub;
    }

    @Getter
    @Setter
    public static class OpfabEntityRecipients {
        private boolean addRscs;
        @Getter(AccessLevel.NONE)
        private String notAllowed;

        public Optional<String> getNotAllowed() {
            return Optional.ofNullable(notAllowed);
        }
    }

    @Setter
    public static class ChangeTimeserieDataDetailValueType {
        private Map<String, ChangeTimeserieDataDetailValueTypeEnum> changeTimeserieDataDetailValueType;

        public Optional<Map<String, ChangeTimeserieDataDetailValueTypeEnum>> getChangeTimeserieDataDetailValueType() {
            return Optional.ofNullable(changeTimeserieDataDetailValueType);
        }
    }

    public enum ChangeTimeserieDataDetailValueTypeEnum {
        INSTANT
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpfabTagsConf {
        private String tag;
        @Getter(AccessLevel.NONE)
        private String qcTagOk;
        @Getter(AccessLevel.NONE)
        private String qcTagWarning;
        @Getter(AccessLevel.NONE)
        private String qcTagError;

        public Optional<String> getQcTagOk() {
            return Optional.ofNullable(qcTagOk);
        }

        public Optional<String> getQcTagWarning() {
            return Optional.ofNullable(qcTagWarning);
        }

        public Optional<String> getQcTagError() {
            return Optional.ofNullable(qcTagError);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpfabFeed {
        private String title;
        private String summary;
    }

    public Optional<Map<String, OpfabTagsConf>> getTags() {
        return Optional.ofNullable(tags);
    }
}
