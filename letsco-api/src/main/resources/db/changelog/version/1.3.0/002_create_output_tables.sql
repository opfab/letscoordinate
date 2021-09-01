-- Copyright (c) 2020-2021, RTE (https://www.rte-france.com)
-- Copyright (c) 2020-2021 RTE international (https://www.rte-international.com)
-- See AUTHORS.txt
-- This Source Code Form is subject to the terms of the Mozilla Public
-- License, v. 2.0. If a copy of the MPL was not distributed with this
-- file, You can obtain one at http://mozilla.org/MPL/2.0/.
-- SPDX-License-Identifier: MPL-2.0
-- This file is part of the Let’s Coordinate project.

ALTER TABLE timeserie DROP COLUMN coordination_status;

ALTER TABLE event_message ADD COLUMN coordination_status VARCHAR(50);

CREATE TABLE event_message_coordination_comment (
                id BIGINT AUTO_INCREMENT NOT NULL,
                id_event_message BIGINT NOT NULL,
                eic_code VARCHAR(50) NOT NULL,
                general_comment VARCHAR(1000) NOT NULL,
                PRIMARY KEY (id)
);

ALTER TABLE event_message_coordination_comment ADD CONSTRAINT event_message_event_message_coordination_comment_fk
FOREIGN KEY (id_event_message)
REFERENCES event_message (id)
ON DELETE CASCADE
ON UPDATE CASCADE;

CREATE TABLE event_message_recipient (
                id BIGINT AUTO_INCREMENT NOT NULL,
                id_event_message BIGINT NOT NULL,
                eic_code VARCHAR(50) NOT NULL,
                PRIMARY KEY (id)
);

ALTER TABLE event_message_recipient ADD CONSTRAINT event_message_event_message_recipient_fk
FOREIGN KEY (id_event_message)
REFERENCES event_message (id)
ON DELETE CASCADE
ON UPDATE CASCADE;

ALTER TABLE timeserie_data_details DROP COLUMN accept;

ALTER TABLE timeserie_data_details DROP COLUMN reject;

ALTER TABLE timeserie_data_details DROP COLUMN explanation;

ALTER TABLE timeserie_data_details DROP COLUMN comment;

CREATE TABLE timeserie_data_details_result (
                id BIGINT AUTO_INCREMENT NOT NULL,
                id_timeserie_data_details BIGINT NOT NULL,
                eic_code VARCHAR(50) NOT NULL,
                answer VARCHAR(50) NOT NULL,
                explanation VARCHAR(1000),
                comment VARCHAR(1000),
                PRIMARY KEY (id)
);

ALTER TABLE timeserie_data_details_result ADD CONSTRAINT timeserie_data_details_timeserie_data_details_result_fk
FOREIGN KEY (id_timeserie_data_details)
REFERENCES timeserie_data_details (id)
ON DELETE CASCADE
ON UPDATE CASCADE;
