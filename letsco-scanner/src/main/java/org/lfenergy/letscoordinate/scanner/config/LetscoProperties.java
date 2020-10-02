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

package org.lfenergy.letscoordinate.scanner.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "letsco")
@Getter
@Setter
public class LetscoProperties {
    private Ftp ftp;
    private Scanner scanner;
    private Backend backend;
    private Scheduler scheduler;


    @Getter
    @Setter
    public static class Ftp {
        private Server server;
        private Path path;
        private boolean createTargetDirIfNotExists;
        private boolean groupMovedFilesInDatedDir;

        @Getter
        @Setter
        public static class Server {
            private String url;
            private Integer port;
            private String username;
            private String password;
        }

        @Getter
        @Setter
        public static class Path {
            private String sourceDownloadDir;
            private String treatedDir;
            private String rejectedDir;
        }
    }

    @Getter
    @Setter
    public static class Scanner {
        private Path path;
        private boolean createTargetDirIfNotExists;

        @Getter
        @Setter
        public static class Path {
            private String targetDownloadDir;
        }
    }

    @Getter
    @Setter
    public static class Backend {
        private String baseUrl;
    }

    @Getter
    @Setter
    public static class Scheduler {
        private Cron cron;
        private FixedRate fixedRate;

        @Getter
        @Setter
        public static class Cron {
            private String downloadFilesCron;
        }

        @Getter
        @Setter
        public static class FixedRate {
            private long fileSizeCheckRateMs;
        }
    }
}
