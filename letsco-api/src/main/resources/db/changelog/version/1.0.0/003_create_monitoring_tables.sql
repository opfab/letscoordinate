-- Copyright (c) 2018-2020, RTE (https://www.rte-france.com)
-- Copyright (c) 2019-2020 RTE international (https://www.rte-international.com)
-- See AUTHORS.txt
-- This Source Code Form is subject to the terms of the Mozilla Public
-- License, v. 2.0. If a copy of the MPL was not distributed with this
-- file, You can obtain one at http://mozilla.org/MPL/2.0/.
-- SPDX-License-Identifier: MPL-2.0
-- This file is part of the Let’s Coordinate project.

CREATE TABLE monitored_task (
                id BIGINT AUTO_INCREMENT NOT NULL,
                task VARCHAR(50) NOT NULL,
                uuid VARCHAR(36) NOT NULL,
                start_time DATETIME(3) NOT NULL,
                end_time DATETIME(3) NOT NULL,
                PRIMARY KEY (id)
);


CREATE TABLE monitored_task_step (
                id BIGINT AUTO_INCREMENT NOT NULL,
                id_monitored_task BIGINT NOT NULL,
                step VARCHAR(50) NOT NULL,
                context TEXT,
                start_time DATETIME(3) NOT NULL,
                end_time DATETIME(3) NOT NULL,
                status VARCHAR(20) NOT NULL,
                comment TEXT,
                comment_details TEXT,
                PRIMARY KEY (id)
);

ALTER TABLE monitored_task_step ADD CONSTRAINT monitored_task_monitored_task_step_fk
FOREIGN KEY (id_monitored_task)
REFERENCES monitored_task (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION;