-- Copyright (c) 2020-2021, RTE (https://www.rte-france.com)
-- Copyright (c) 2020-2021 RTE international (https://www.rte-international.com)
-- See AUTHORS.txt
-- This Source Code Form is subject to the terms of the Mozilla Public
-- License, v. 2.0. If a copy of the MPL was not distributed with this
-- file, You can obtain one at http://mozilla.org/MPL/2.0/.
-- SPDX-License-Identifier: MPL-2.0
-- This file is part of the Let’s Coordinate project.

CREATE TABLE coordination (
                id BIGINT AUTO_INCREMENT NOT NULL,
                id_event_message BIGINT UNIQUE NOT NULL,
                process_key VARCHAR(250) NOT NULL,
                publish_date DATETIME(3) NOT NULL,
                start_date DATETIME(3) NOT NULL,
                end_date DATETIME(3) NOT NULL,
                status VARCHAR(20),
                PRIMARY KEY (id)
);

CREATE TABLE coordination_general_comment (
                id BIGINT AUTO_INCREMENT NOT NULL,
                id_coordination BIGINT NOT NULL,
                eic_code VARCHAR(20) NOT NULL,
                general_comment VARCHAR(1000) NOT NULL,
                PRIMARY KEY (id)
);

CREATE TABLE coordination_ra (
                id BIGINT AUTO_INCREMENT NOT NULL,
                id_coordination BIGINT NOT NULL,
                event VARCHAR(500),
                constraintt VARCHAR(500),
                remedial_action VARCHAR(500) NOT NULL,
                PRIMARY KEY (id)
);

CREATE TABLE coordination_ra_answer (
                id BIGINT AUTO_INCREMENT NOT NULL,
                id_coordination_ra BIGINT NOT NULL,
                eic_code VARCHAR(20) NOT NULL,
                answer VARCHAR(3) NOT NULL,
                explanation VARCHAR(500),
                comment VARCHAR(500),
                PRIMARY KEY (id)
);


ALTER TABLE coordination ADD CONSTRAINT event_message_coordination_fk
FOREIGN KEY (id_event_message)
REFERENCES event_message (id)
ON DELETE CASCADE
ON UPDATE CASCADE;

ALTER TABLE coordination_general_comment ADD CONSTRAINT coordination_coordination_general_comment_fk
FOREIGN KEY (id_coordination)
REFERENCES coordination (id)
ON DELETE CASCADE
ON UPDATE CASCADE;

ALTER TABLE coordination_ra ADD CONSTRAINT coordination_coordination_ra_fk
FOREIGN KEY (id_coordination)
REFERENCES coordination (id)
ON DELETE CASCADE
ON UPDATE CASCADE;

ALTER TABLE coordination_ra_answer ADD CONSTRAINT coordination_coordination_ra_answer_fk
FOREIGN KEY (id_coordination_ra)
REFERENCES coordination_ra (id)
ON DELETE CASCADE
ON UPDATE CASCADE;