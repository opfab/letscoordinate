
  
**WARNING : THIS SOFTWARE IS NO LONGER BEING MAINTAINED**

<!-- Copyright (c) 2020-2021 RTE (https://www.rte-france.com)                                                  -->
<!-- Copyright (c) 2020-2021 RTE international (https://www.rte-international.com)                             -->
<!-- See AUTHORS.txt                                                                                      -->
<!-- This document is subject to the terms of the Creative Commons Attribution 4.0 International license. -->
<!-- If a copy of the license was not distributed with this                                               -->
<!-- file, You can obtain one at https://creativecommons.org/licenses/by/4.0/.                            -->
<!-- SPDX-License-Identifier: CC-BY-4.0                                                                   -->

# Let's Coordinate - Getting started
---

## 1. Prerequisites

To use Let's Coordinate, you need a linux OS with the following:

* Maven (v3.5.3)
* Java (v11.0.10)
* Docker (v18.09.9)
* Docker-compose (v1.22)
* NPM (v7.5.2) 
* Node JS (v10.16.3)
* Angular CLI (v11.1.4)

**Please note**: 
* It is highly recommended to use [sdkman](https://sdkman.io/) (v5.11.0 or grater) and [nvm](https://github.com/nvm-sh/nvm) (v14.11.0 or grater) to manage *Maven*, *Java*, *NPM* and *Node JS* tools versions (with sdkman and nvm, the previously mentioned tools will be automatically installed later).
* The required OperatorFabric version is **2.10.0.RELEASE** (configured by default to be used with the current version of Let's Coordinate 1.3.1.RELEASE)

## 2. Setup and run Let's Coordinate

#### 2.1. Clone Let's Coordinate project

To start Let's Coordinate, you first need to clone the *letscoordinate* git project:

```
git clone https://github.com/opfab/letscoordinate.git
```

#### 2.2. Create test branch

Before starting Let's Coordinate, it's recommended to create a test branch from the latest stable release (1.3.1.RELEASE in our case).
To do this, you first need to make sure that you have the latest tag list from your remote repository:

```
git fetch --all --tags
```

Then, you can create the test branch:

```
git checkout tags/1.3.1.RELEASE -b test-letsco-1.3.1.RELEASE
```

#### 2.3. Run Let's Coordinate

##### a. Prepare the environment

Before starting the *Let's Coordinate* getting started project, it is important to mention that we use the "*karate.jar*" dependency to initialize our database with required test data.

Download the latest karate jar file from [Karate github release page](https://github.com/intuit/karate/releases/) , put it in the "*opfab/prepare-opfab-env*" directory, rename it to "*karate.jar*" to use it easily.

Then, position your self in the "*bin*" directory and check that you have the right "*SERVER_IP*" value in the "*load_environment.sh*" file (this action is mandatory only if you are running *Let's Coordinate* in a remote server! If it is the case, please edit the file and replace "*localhost*" by your server IP address, else if you are testing locally, please skip this step):

```
sed -n '13,+6p' load_environment.sh
```

Finally, in the same "*bin*" directory, you should load the environment variables by executing the following command:

```
source ./load_environment.sh
```

##### b. Start the server

To start Let's Coordinate, position your self in the "*bin*" directory and execute the following commands:

```
./server.sh --build start 
```

This command (with ```--build``` or ```-b``` option) allows to:
* install the OperatorFabric required dependencies
* build and deploy the Let's Coordinate docker images locally

When the first command is executed correctly, please run the following command:

```
./server.sh --init start 
```

This command (with ```--init``` or ```-i``` option) allows to:
* send bundles and configuration files to the OperatorFabric server,
* initialize the OperatorFabric database with required data (users, groups, perimeters, entities, ...)

For next usage of the ```server.sh``` script, the ```--build``` and ```--init``` options are not necessary.

To be sure that all the services are correctly started, you can try the following command:

```
./server.sh status 
```

To see more about the ```server.sh``` commands and options, please try:
```
./server.sh --help
```

## 3. Use Let's Coordinate

#### 3.1. Generate a token

To be able to use the secured APIs of the letsco-api module (e.g. to send notifications, to generate an RSC KPI report, ...), you first need to generate a new token:

- Go to the following url (letsco-api swagger-ui): [http://localhost:8088/swagger-ui.html](http://localhost:8088/swagger-ui.html)
- Click on *authentication-controller*
- Click on *GET /letsco/api/v1/auth/token*
- Click on *Try it out*
- Set **user.test** as username and **test** as password
- Click on *Execute* 

The result returned in the "Response body" is the token that we will use in the next sections for authentication (by default the generated tokens are valid for 24 hours. After exceeding this period, 
a new token should be generated)

#### 3.2. Send a new card 

- Go to the following url (letsco-api swagger-ui): [http://localhost:8088/swagger-ui.html](http://localhost:8088/swagger-ui.html)
- Click on *event-message-controller*
- Click on *POST /letsco/api/v1/upload/save*
- Click on *Try it out*
- Copy the previous generated token (see section 3.1) and past it in the *Authorization* field (be sure that the copied text starts with the "Bearer" keyword!)
- Click the "*Browse...*" button and choose one of the files from the directory "*util/messages_models/json/card_feed*".
- Click on *Execute*

If you open a browser and connect to application ([http://localhost/ui/](http://localhost/ui/)) with the username **user.test** and password **test**, you should see the new card in the Feed (if it is not the case, please change the timeline view or/and period to include the card's dates: timestamp or/and business period).

Feel free to test the other json samples from the directory "*util/messages_models/json/card_feed*" 

#### 3.3. Generate a RSC KPI report

- Go to the following url (letsco-api swagger-ui): [http://localhost:8088/swagger-ui.html](http://localhost:8088/swagger-ui.html)
- Click on *event-message-controller*
- Click on *POST /letsco/api/v1/upload/save*
- Click on *Try it out*
- Copy the previous generated token (see section 3.1) and past it in the *Authorization* field (be sure that the copied text starts with the "Bearer" keyword!)
- Click on the "*Browse...*" button and choose the file "*util/messages_models/json/rsc_kpi_report/kpi_use_cases_service_a.json*".
- Click on *Execute*
- Click again on the "*Browse...*" button and choose the file "*util/messages_models/json/rsc_kpi_report/kpi_use_cases_service_b.json*".
- Click on *Execute*
- Connect to OpFab ([http://localhost/ui/](http://localhost/ui/)) with the username **user.test** and password **test**.
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

If you want to stop the application and definitely remove all docker containers, you can use the following command:
```
./server.sh down
```
