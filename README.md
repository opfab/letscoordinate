<!-- Copyright (c) 2020 RTE (https://www.rte-france.com)                                                  -->
<!-- Copyright (c) 2020 RTE international (https://www.rte-international.com)                             -->
<!-- See AUTHORS.txt                                                                                      -->
<!-- This document is subject to the terms of the Creative Commons Attribution 4.0 International license. -->
<!-- If a copy of the license was not distributed with this                                               -->
<!-- file, You can obtain one at https://creativecommons.org/licenses/by/4.0/.                            -->
<!-- SPDX-License-Identifier: CC-BY-4.0                                                                   -->

# Let's Coordinate - Getting started

## 1. Prerequisites

To use Let's Coordinate, you need a linux OS with the following:

* Maven
* Java (8 or grater)
* Docker
* Docker-compose
* NPM (6 or grater) 
* Angular CLI (8 or grater)

## 2. Setup and run Let's Coordinate

#### 2.1. Clone Let's Coordinate project

To start Let's Coordinate, you first need to clone the *letscoordinate* git project:
```
git clone https://github.com/opfab/letscoordinate.git
```

#### 2.2. Run Operator Fabric

Clone the *operatorfabric-getting-started* git project:
```
git clone https://github.com/opfab/operatorfabric-getting-started.git
```

Copy files "*web-ui.json*", "*docker-compose.yml*" and "*favicon.ico*" from "*letscoordinate/test/prepare-opfab-env/opfab-config*" directory and past them into "*operatorfabric-getting-started/server*". Override the existing files when asked!

Launch the *startserver.sh* in the server directory. You need to wait for all the services to start (it usually takes one minute to start), it is done when no more logs are written on the output (It could continue to log but slowly).

#### 2.3. Run Let's Coordinate backend

Position yourself in the *bin* directory of the Let’s Coordinate project and run the following command:

```
./run_letscoos.sh
```

This will start running the docker containers Kafka (*port 9092*), Zookeeper (*port 2181*), MariaDB (*port 3306*), the Let's Co data provider app. (*letsco-data-provider*) and the Let's Co main application (*letsco-api*).

#### 2.4. Run Let's Coordinate frontend

Change directory to the *letsco-front* module and execute the following commands:

```
npm install
ng serve
```

#### 2.5. Prepare the environment

##### a. Update the OpFab database

Position yourself in the root directory of the Let's Coordinate project and run the following command:

```
./test/prepare-opfab-env/prepare-opfab-env.sh
```

This will create a new group, a new entity, and a new user associated to this group and this entity in the operator-fabric database, and send the bundles to display the cards in OperatorFabric.

##### b. Add the user to keycloak

- On your browser, go to the keycloak admin url: [http://localhost:89/auth/](http://localhost:89/auth/)
- Click on *Administration console*
- Connect using the following credentials: **user** *admin*, **password** *admin*
- In the left menu, go to *Users*
- Click on *Add user*
- Fill the **Username** field with "*user.test*", **First Name** field with "*User*", **Last Name** field with "*Test*" and then click on *Save*
- Go to *Credentials*
- Fill the password with whatever you want, put *Temporary* to *OFF* and click on *Reset Password*

## 3. Use Let's Coordinate

#### 3.1. Send a new notification

- Go to the following url: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
- Click on *kafka-producer-controller*
- Click on *POST /letsco/data-provider/v1/kafka/json/raw-msg*
- Click on *Try it out*
- In the data body value, put the content of one of the files from the directory *message_models/json/card_feed/* (e.g: *ProcessSuccess.json*, *MessageValidated_NEGATIVE_ACK.json*, ...)
- Click on *Execute*

If you connect to OpFab ([http://localhost:2002/ui/](http://localhost:2002/ui/)) with the user previously created in Keycloak, you should see the new card in the Feed.

Feel free to test the other json samples from the directory *message_models/json/card_feed/* 

#### 3.2. Generate a RSC KPI report

- Go to the following url: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
- Click on *kafka-producer-controller*
- Click on *POST /letsco/data-provider/v1/kafka/json/raw-msg*
- Click on *Try it out*
- In the data body value, put the content of the file *message_models/json/rsc_kpi_report/kpi_use_cases.json*
- Click on *Execute*
- Connect to OpFab ([http://localhost:2002/ui/](http://localhost:2002/ui/)) with the user previously created in Keycloak.
- Select the *"RSC KPI Report"* menu.
- Select the *period*, *RSC*, *RSC Service* and *Data type* and then click the *submit* button, you should see the generated RSC KPI Report.

## 4. Stop the application

Position yourself in the *bin* directory of Let's Coordinate project and run the following command:

```
./bin/run_letscoos.sh stop
```

This will stop the *letsco-api* & *letsco-data-provider* processes, and stop the docker services (*Kafka*, *Zookeeper*, *MariaDB*).
