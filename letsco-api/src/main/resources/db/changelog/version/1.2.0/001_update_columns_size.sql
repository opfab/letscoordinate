-- Copyright (c) 2020, RTE (https://www.rte-france.com)
-- Copyright (c) 2020 RTE international (https://www.rte-international.com)
-- See AUTHORS.txt
-- This Source Code Form is subject to the terms of the Mozilla Public
-- License, v. 2.0. If a copy of the MPL was not distributed with this
-- file, You can obtain one at http://mozilla.org/MPL/2.0/.
-- SPDX-License-Identifier: MPL-2.0
-- This file is part of the Let’s Coordinate project.

ALTER TABLE event_message MODIFY message_id VARCHAR(100) NOT NULL;
ALTER TABLE event_message MODIFY noun VARCHAR(250) NOT NULL;
ALTER TABLE event_message MODIFY verb VARCHAR(100) NOT NULL;
ALTER TABLE event_message MODIFY source VARCHAR(250) NOT NULL;
ALTER TABLE event_message MODIFY message_type VARCHAR(100);
ALTER TABLE event_message MODIFY message_type_name VARCHAR(250) NOT NULL;
ALTER TABLE event_message MODIFY sending_user VARCHAR(50);
ALTER TABLE event_message MODIFY process_step VARCHAR(100);
ALTER TABLE event_message MODIFY timeframe VARCHAR(50);

ALTER TABLE rsc_kpi_data MODIFY label VARCHAR(250);

ALTER TABLE text MODIFY name VARCHAR(250) NOT NULL;

ALTER TABLE link MODIFY name VARCHAR(250) NOT NULL;

ALTER TABLE link_eic_code MODIFY eic_code VARCHAR(50) NOT NULL;

ALTER TABLE timeserie MODIFY name VARCHAR(250) NOT NULL;

ALTER TABLE timeserie_data_details MODIFY identifier VARCHAR(100);
ALTER TABLE timeserie_data_details MODIFY label VARCHAR(250);
ALTER TABLE timeserie_data_details MODIFY value VARCHAR(1000) NOT NULL;
ALTER TABLE timeserie_data_details MODIFY explanation VARCHAR(1000);
ALTER TABLE timeserie_data_details MODIFY comment VARCHAR(1000);

ALTER TABLE timeserie_data_details_eic_code MODIFY eic_code VARCHAR(50) NOT NULL;

ALTER TABLE user MODIFY eic_code VARCHAR(50);

ALTER TABLE user_service MODIFY service_code VARCHAR(250) NOT NULL;