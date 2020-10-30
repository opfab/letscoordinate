<!-- Copyright (c) 2020 RTE (https://www.rte-france.com)                                                  -->
<!-- Copyright (c) 2020 RTE international (https://www.rte-international.com)                             -->
<!-- See AUTHORS.txt                                                                                      -->
<!-- This document is subject to the terms of the Creative Commons Attribution 4.0 International license. -->
<!-- If a copy of the license was not distributed with this                                               -->
<!-- file, You can obtain one at https://creativecommons.org/licenses/by/4.0/.                            -->
<!-- SPDX-License-Identifier: CC-BY-4.0                                                                   -->

# Let's Coordinate - Getting started
<br/>

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

> /!\ The minimal required version of OperatorFabric is: **1.7.0.RELEASE**

Clone the *operatorfabric-getting-started* git project:
```
git clone https://github.com/opfab/operatorfabric-getting-started.git
```

Copy files "*docker-compose.yml*", "*favicon.ico*", "*ngnix.conf*" and "*web-ui.json*" from "*letscoordinate/test/prepare-opfab-env/opfab-config*" directory and past them into "*operatorfabric-getting-started/server*". Overwrite the existing files when asked!

In the directory "*operatorfabric-getting-started/server*" launch the following command:
```
./startServer.sh 
```

You need to wait for all the services to start (it usually takes one minute to start), it is done when no more logs are written on the output (It could continue to log but slowly).

#### 2.3. Run Let's Coordinate backend

In a new terminal, position yourself in the "*letscoordinate/bin*" directory and run the following command:

```
./run_letscoos.sh
```

This will start running the docker containers Kafka (*port 9092*), Zookeeper (*port 2181*), MariaDB (*port 3306*), the *letsco-data-provider* application (*port 8082*) and *letsco-api* application (*port 8088*).

#### 2.4. Run Let's Coordinate frontend

In a new terminal, change directory to the "*letscoordinate/letsco-front*" module and execute the following commands:

```
npm install
ng serve
```

#### 2.5. Prepare the environment

##### a. Update the OpFab database

Download the latest *karate.jar* from [Karate github release page](https://github.com/intuit/karate/releases/)

Put it in the "*letscoordinate/test/prepare-opfab-env*" directory, rename it to "*karate.jar*" to use it easily.

Position yourself in the "*letscoordinate*" root directory and run the following command:

```
./test/prepare-opfab-env/prepare-opfab-env.sh
```

This will create a new group, a new entity, and a new user associated to this group and this entity in the *operator-fabric* database, and will send the bundles to display the cards in OperatorFabric.

##### b. Add the user to keycloak

- On your browser, go to the keycloak admin url: [http://localhost:89/auth/](http://localhost:89/auth/)
- Click on *Administration console*
- Connect using the following credentials: **user** *admin*, **password** *admin*
- In the left menu, go to *Users*
- Click on *Add user*
- Fill the **Username** field with "*user.test*", **First Name** field with "*User*", **Last Name** field with "*Test*" and then click on *Save*
- Go to *Credentials*
- Fill the password with whatever you want, put *Temporary* to *OFF* and click on *Reset Password*
- In the opened popup, click on *Change password*

## 3. Use Let's Coordinate

#### 3.1. Send a new notification

- Go to the following url: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
- Click on *kafka-producer-controller*
- Click on *POST /letsco/data-provider/v1/kafka/json/raw-msg*
- Click on *Try it out*
- In the data body value, put the content of one of the files from the directory "*letscoordinate/message_models/json/card_feed/*" (e.g: *ProcessSuccess.json*, *MessageValidated_NEGATIVE_ACK.json*, ...)
- Click on *Execute*

If you connect to OpFab ([http://localhost:2002/ui/](http://localhost:2002/ui/)) with the user previously created in Keycloak, you should see the new card in the Feed.

Feel free to test the other json samples from the directory "*letscoordinate/message_models/json/card_feed/*" 

#### 3.2. Generate a RSC KPI report

- Go to the following url: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
- Click on *kafka-producer-controller*
- Click on *POST /letsco/data-provider/v1/kafka/json/raw-msg*
- Click on *Try it out*
- In the data body value, put the content of the file "*letscoordinate/message_models/json/rsc_kpi_report/kpi_use_cases.json*"
- Click on *Execute*
- Connect to OpFab ([http://localhost:2002/ui/](http://localhost:2002/ui/)) with the user previously created in Keycloak.
- Select the *"RSC KPI Report"* menu.
- Select the *period*, *RSC*, *RSC Service* and *Data type* and then click the *submit* button, you should see the generated RSC KPI Report.

## 4. Stop the application

To stop the application, we should start by stopping OperatorFabric and then Let's Coordinate.

#### 4.1. Stop OperatorFabric

To stop OperatorFabric server, change directory to "*operatorfabric-getting-started/server*" and execute the following command:

```
docker-compose stop &
```

#### 4.2. Stop Let's Coordinate

Position yourself in the "*letscoordinate/bin*" directory and run the following command:

```
./run_letscoos.sh stop
```

This will stop the *letsco-api* & *letsco-data-provider* processes, and stop the docker services (*Kafka*, *Zookeeper*, *MariaDB*).
