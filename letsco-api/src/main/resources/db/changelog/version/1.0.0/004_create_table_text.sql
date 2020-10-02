-- Copyright (c) 2018-2020, RTE (https://www.rte-france.com)
-- Copyright (c) 2019-2020 RTE international (https://www.rte-international.com)
-- See AUTHORS.txt
-- This Source Code Form is subject to the terms of the Mozilla Public
-- License, v. 2.0. If a copy of the MPL was not distributed with this
-- file, You can obtain one at http://mozilla.org/MPL/2.0/.
-- SPDX-License-Identifier: MPL-2.0
-- This file is part of the Let’s Coordinate project.

CREATE TABLE text (
                id BIGINT AUTO_INCREMENT NOT NULL,
                id_event_message BIGINT NOT NULL,
                name VARCHAR(50) NOT NULL,
                value TEXT NOT NULL,
                PRIMARY KEY (id)
);

ALTER TABLE text ADD CONSTRAINT event_message_text_fk
FOREIGN KEY (id_event_message)
REFERENCES event_message (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION;