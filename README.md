<!-- Copyright (c) 2020 RTE (https://www.rte-france.com)                                                  -->
<!-- Copyright (c) 2020 RTE international (https://www.rte-international.com)                             -->
<!-- See AUTHORS.txt                                                                                      -->
<!-- This document is subject to the terms of the Creative Commons Attribution 4.0 International license. -->
<!-- If a copy of the license was not distributed with this                                               -->
<!-- file, You can obtain one at https://creativecommons.org/licenses/by/4.0/.                            -->
<!-- SPDX-License-Identifier: CC-BY-4.0                                                                   -->

# Let's Coordinate - Getting started
---

## 1. Prerequisites

To use Let's Coordinate, you need a linux OS with the following:

* Maven (3.5.3)
* Java (8 or grater)
* Docker (18.09.9)
* Docker-compose (1.27.4)
* NPM (v6.9.0) 
* Node JS (v10.16.3)
* Angular CLI (8 or grater)

**Please note**: It is highly recommended to use [sdkman](https://sdkman.io/) and [nvm](https://github.com/nvm-sh/nvm) to manage tools versions (Maven, NPM and Node JS). 

## 2. Setup and run Let's Coordinate

#### 2.1. Clone Let's Coordinate project

To start Let's Coordinate, you first need to clone the *letscoordinate* git project:

```
git clone https://github.com/opfab/letscoordinate.git
```

#### 2.2. Create test branch

Before starting Let's Coordinate, it's recommended to create a test branch from the latest stable release (1.2.0.RELEASE in our case).
To do this, you first need to make sure that you have the latest tag list from your remote repository:

```
git fetch --all --tags
```

Then, you can create the test branch:

```
git checkout tags/1.2.0.RELEASE -b test-letsco-1.2.0.RELEASE
```

#### 2.3. Run Let's Coordinate

##### a. Prepare the environment

Before starting the *Let's Coordinate* getting started project, it is important to mention that we use the *karate.jar* dependency to initialize our database with required test data.

Download the latest *karate.jar* from [Karate github release page](https://github.com/intuit/karate/releases/) , put it in the "*opfab/prepare-opfab-env*" directory, rename it to "*karate.jar*" to use it easily.

After that, you have to add the *keycloak*, *cards-publication*, *businessconfig*, *web-ui*, *kafka*, *letsco-front* and *letsco-api* hosts to the */etc/hosts* file in order to map the host names with the corresponding docker containers address (you can change the *127.0.0.1* ip address by the one you use to connect to your docker containers if different):

```
127.0.0.1   keycloak cards-publication businessconfig web-ui kafka letsco-front letsco-api
```

One more thing to do before starting the server, is to load the environment variables. To do that, position your self in the root directory of the *Let's Coordinate* project (by default letscoordinate) and execute the following command:

```
source bin/load_environment.sh
```

##### b. Start the server

To start Let's Coordinate, position your self in the *bin* directory and execute the following commands:

```
./server.sh --first-init start 
```

The *--first-init* (or -f) option is required for the first test of the project, it allows to:
* install the OperatorFabric required dependencies, 
* build and deploy the Let's Coordinate docker images locally,
* send bundles and configurations to the OperatorFabric server,
* initialize the OperatorFabric database with required data (users, groups, perimeters, entities, ...)

For next usage of the *server.sh* script, the *--first-init* option is not necessary.

To be sure that all the services are correctly started, you can try the following command:

```
./server.sh status 
```

To see more about the server.sh commands and options, please try:
```
./server.sh --help
```

## 3. Use Let's Coordinate

#### 3.1. Send a new card

- Go to the following url (letsco-data-provider swagger-ui): [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
- Click on *kafka-producer-controller*
- Click on *POST /letsco/data-provider/v1/kafka/json/raw-msg*
- Click on *Try it out*
- In the data body value, put the content of one of the files from the directory "*message_models/json/card_feed/*" (e.g: *ProcessSuccessful.json*, *MessageValidated_NEGATIVE_ACK.json*, ...)
- Click on *Execute*

If you open a browser and connect to application ([http://localhost/ui/](http://localhost/ui/)) with the user [username:**user.test**/password:**test**], you should see the new card in the Feed (if it is not the case, please change the timeline view or/and period to include the card's dates: timestamp or/and business period).

Feel free to test the other json samples from the directory "*message_models/json/card_feed/*" 

#### 3.2. Generate a RSC KPI report

- Go to the following url (letsco-api swagger-ui): [http://localhost:8088/swagger-ui.html](http://localhost:8088/swagger-ui.html)
- Click on *authentication-controller*
- Click on *GET /letsco/api/v1/auth/token*
- Click on *Try it out*
- Set **user.test** as username and **test** as password
- Click on *Execute* 
- Copy the generated token (with the Bearer word)
- Click on *event-message-controller*
- Click on *POST /letsco/api/v1/upload/save*
- Click on *Try it out*
- Past the previous copied token in the *Authorization* field
- Click the *Browse...* button and choose the "*message_models/json/rsc_kpi_report/kpi_use_cases.json*" file.
- Click on *Execute*
- Connect to OpFab ([http://localhost/ui/](http://localhost/ui/)) with the user [username:**user.test**/password:**test**].
- Select the *"RSC KPI Report"* menu.
- Select the *RSC Service*, *Period*, *RSC* or *Region* and *Data type* and then click the *submit* button, you should see the generated RSC KPI Report.

## 4. Stop the application

To stop the application, position yourself in the *bin* directory and run the following command:

```
./server.sh stop
```

To be sure that all the services are correctly stopped, you can try the following command:

```
./server.sh status 
```

If you want to stop the application and definitely remove all the docker images, you can try the following command:
```
./server.sh down
```