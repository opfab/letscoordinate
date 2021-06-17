-- Copyright (c) 2020-2021, RTE (https://www.rte-france.com)
-- Copyright (c) 2020-2021 RTE international (https://www.rte-international.com)
-- See AUTHORS.txt
-- This Source Code Form is subject to the terms of the Mozilla Public
-- License, v. 2.0. If a copy of the MPL was not distributed with this
-- file, You can obtain one at http://mozilla.org/MPL/2.0/.
-- SPDX-License-Identifier: MPL-2.0
-- This file is part of the Let’s Coordinate project.

CREATE TABLE event_message_file (
    id BIGINT AUTO_INCREMENT NOT NULL,
    id_event_message BIGINT NOT NULL,
    file LONGBLOB NOT NULL,
    PRIMARY KEY (ID)
);

ALTER TABLE event_message_file ADD CONSTRAINT event_message_event_message_file_fk
FOREIGN KEY (id_event_message)
REFERENCES event_message (id)
ON DELETE CASCADE
ON UPDATE CASCADE;

ALTER TABLE event_message ADD COLUMN unique_file_identifier VARCHAR(36);