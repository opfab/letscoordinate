-- Copyright (c) 2020-2021, RTE (https://www.rte-france.com)
-- Copyright (c) 2020-2021 RTE international (https://www.rte-international.com)
-- See AUTHORS.txt
-- This Source Code Form is subject to the terms of the Mozilla Public
-- License, v. 2.0. If a copy of the MPL was not distributed with this
-- file, You can obtain one at http://mozilla.org/MPL/2.0/.
-- SPDX-License-Identifier: MPL-2.0
-- This file is part of the Let’s Coordinate project.

ALTER TABLE rsc_kpi DROP FOREIGN KEY event_message_rsc_kpi_fk;

ALTER TABLE rsc_kpi ADD CONSTRAINT event_message_rsc_kpi_fk
FOREIGN KEY (id_event_message)
REFERENCES event_message (id)
ON DELETE CASCADE
ON UPDATE CASCADE;

ALTER TABLE rsc_kpi_data DROP FOREIGN KEY rsc_kpi_rsc_kpi_data_fk;

ALTER TABLE rsc_kpi_data ADD CONSTRAINT rsc_kpi_rsc_kpi_data_fk
FOREIGN KEY (id_rsc_kpi)
REFERENCES rsc_kpi (id)
ON DELETE CASCADE
ON UPDATE CASCADE;

ALTER TABLE rsc_kpi_data_details DROP FOREIGN KEY rsc_kpi_data_rsc_kpi_data_details_fk;

ALTER TABLE rsc_kpi_data_details ADD CONSTRAINT rsc_kpi_data_rsc_kpi_data_details_fk
FOREIGN KEY (id_rsc_kpi_data)
REFERENCES rsc_kpi_data (id)
ON DELETE CASCADE
ON UPDATE CASCADE;

ALTER TABLE monitored_task_step DROP FOREIGN KEY monitored_task_monitored_task_step_fk;

ALTER TABLE monitored_task_step ADD CONSTRAINT monitored_task_monitored_task_step_fk
FOREIGN KEY (id_monitored_task)
REFERENCES monitored_task (id)
ON DELETE CASCADE
ON UPDATE CASCADE;

ALTER TABLE text DROP FOREIGN KEY event_message_text_fk;

ALTER TABLE text ADD CONSTRAINT event_message_text_fk
FOREIGN KEY (id_event_message)
REFERENCES event_message (id)
ON DELETE CASCADE
ON UPDATE CASCADE;

ALTER TABLE link DROP FOREIGN KEY event_message_link_fk;

ALTER TABLE link ADD CONSTRAINT event_message_link_fk
FOREIGN KEY (id_event_message)
REFERENCES event_message (id)
ON DELETE CASCADE
ON UPDATE CASCADE;

ALTER TABLE link_eic_code DROP FOREIGN KEY link_link_eic_code_fk;

ALTER TABLE link_eic_code ADD CONSTRAINT link_link_eic_code_fk
FOREIGN KEY (id_link)
REFERENCES link (id)
ON DELETE CASCADE
ON UPDATE CASCADE;

ALTER TABLE timeserie DROP FOREIGN KEY event_message_timeserie_fk;

ALTER TABLE timeserie ADD CONSTRAINT event_message_timeserie_fk
FOREIGN KEY (id_event_message)
REFERENCES event_message (id)
ON DELETE CASCADE
ON UPDATE CASCADE;

ALTER TABLE timeserie_data DROP FOREIGN KEY timeserie_timeserie_data_fk;

ALTER TABLE timeserie_data ADD CONSTRAINT timeserie_timeserie_data_fk
FOREIGN KEY (id_timeserie)
REFERENCES timeserie (id)
ON DELETE CASCADE
ON UPDATE CASCADE;

ALTER TABLE timeserie_data_details DROP FOREIGN KEY timeserie_data_timeserie_data_details_fk;

ALTER TABLE timeserie_data_details ADD CONSTRAINT timeserie_data_timeserie_data_details_fk
FOREIGN KEY (id_timeserie_data)
REFERENCES timeserie_data (id)
ON DELETE CASCADE
ON UPDATE CASCADE;

ALTER TABLE timeserie_data_details_eic_code DROP FOREIGN KEY timeserie_data_details_timeserie_data_details_eic_code_fk;

ALTER TABLE timeserie_data_details_eic_code ADD CONSTRAINT timeserie_data_details_timeserie_data_details_eic_code_fk
FOREIGN KEY (id_timeserie_data_details)
REFERENCES timeserie_data_details (id)
ON DELETE CASCADE
ON UPDATE CASCADE;

ALTER TABLE user_service DROP FOREIGN KEY user_user_service_fk;

ALTER TABLE user_service ADD CONSTRAINT user_user_service_fk
FOREIGN KEY (id_user)
REFERENCES user (id)
ON DELETE CASCADE
ON UPDATE CASCADE;