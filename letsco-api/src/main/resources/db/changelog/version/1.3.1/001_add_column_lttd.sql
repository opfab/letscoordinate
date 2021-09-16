-- Copyright (c) 2020-2021, RTE (https://www.rte-france.com)
-- Copyright (c) 2020-2021 RTE international (https://www.rte-international.com)
-- See AUTHORS.txt
-- This Source Code Form is subject to the terms of the Mozilla Public
-- License, v. 2.0. If a copy of the MPL was not distributed with this
-- file, You can obtain one at http://mozilla.org/MPL/2.0/.
-- SPDX-License-Identifier: MPL-2.0
-- This file is part of the Let’s Coordinate project.

ALTER TABLE coordination ADD COLUMN lttd DATETIME(3);

CREATE TABLE coordination_lttd_queue (
                id BIGINT AUTO_INCREMENT NOT NULL,
                id_coordination BIGINT NOT NULL,
                lttd DATETIME(3),
                PRIMARY KEY (id)
);

ALTER TABLE coordination_lttd_queue ADD CONSTRAINT coordination_lttd_queue_fk
FOREIGN KEY (id_coordination)
REFERENCES coordination (id)
ON DELETE CASCADE
ON UPDATE CASCADE;