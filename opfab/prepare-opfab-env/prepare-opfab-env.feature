# Copyright (c) 2020, RTE (https://www.rte-france.com)
# Copyright (c) 2020 RTE international (https://www.rte-international.com)
# See AUTHORS.txt
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# SPDX-License-Identifier: MPL-2.0
# This file is part of the Let’s Coordinate project.

Feature: Prepare OpFab env. for Let's Co open source

#################################################
#            GROUPS AND PERIMETERS              #
#################################################

  Background:
    * def signIn = call read('./getToken.feature') { username: 'admin'}
    * def authToken = signIn.authToken

  Scenario: Create group Service A

    * def group =
"""
{
  "id" : "servicea",
  "name" : "Service A",
  "description" : "The Service A group"
}
"""

    Given url opfabUrl + 'users/groups'
    And header Authorization = 'Bearer ' + authToken
    And request group
    When method post
    Then assert responseStatus == 200 || responseStatus == 201
    And match response.description == group.description
    And match response.name == group.name
    And match response.id == group.id

  Scenario: Create group Service B

    * def group =
"""
{
  "id" : "serviceb",
  "name" : "Service B",
  "description" : "The Service B group"
}
"""

    Given url opfabUrl + 'users/groups'
    And header Authorization = 'Bearer ' + authToken
    And request group
    When method post
    Then assert responseStatus == 200 || responseStatus == 201
    And match response.description == group.description
    And match response.name == group.name
    And match response.id == group.id

  Scenario: Create perimeter for Service A Process Monitoring

    * def serviceAProcessMonitoringPerimeter =
"""
{
  "id" : "serviceAProcessMonitoringPerimeter",
  "process" : "servicea_processmonitoring",
  "stateRights" : [
    {
      "state" : "processsuccessful",
      "right" : "ReceiveAndWrite"
    },
    {
      "state" : "processfailed",
      "right" : "ReceiveAndWrite"
    }
  ]
}
"""

    Given url opfabUrl + 'users/perimeters'
    And header Authorization = 'Bearer ' + authToken
    And request serviceAProcessMonitoringPerimeter
    When method post
    Then assert responseStatus == 201 || (responseStatus == 400 && response.errors[0] == "Duplicate key : serviceAProcessMonitoringPerimeter")

  Scenario: Create perimeter for Service A Validation File A

    * def serviceAValidationFileAPerimeter =
"""
{
  "id" : "serviceAValidationFileAPerimeter",
  "process" : "servicea_validation_filea",
  "stateRights" : [
    {
      "state" : "ok",
      "right" : "ReceiveAndWrite"
    },
    {
      "state" : "warning",
      "right" : "ReceiveAndWrite"
    },
    {
      "state" : "error",
      "right" : "ReceiveAndWrite"
    }
  ]
}
"""

    Given url opfabUrl + 'users/perimeters'
    And header Authorization = 'Bearer ' + authToken
    And request serviceAValidationFileAPerimeter
    When method post
    Then assert responseStatus == 201 || (responseStatus == 400 && response.errors[0] == "Duplicate key : serviceAValidationFileAPerimeter")

  Scenario: Create perimeter for Service A Validation File B

    * def serviceAValidationFileBPerimeter =
"""
{
  "id" : "serviceAValidationFileBPerimeter",
  "process" : "servicea_validation_fileb",
  "stateRights" : [
    {
      "state" : "ok",
      "right" : "ReceiveAndWrite"
    },
    {
      "state" : "warning",
      "right" : "ReceiveAndWrite"
    },
    {
      "state" : "error",
      "right" : "ReceiveAndWrite"
    }
  ]
}
"""

    Given url opfabUrl + 'users/perimeters'
    And header Authorization = 'Bearer ' + authToken
    And request serviceAValidationFileBPerimeter
    When method post
    Then assert responseStatus == 201 || (responseStatus == 400 && response.errors[0] == "Duplicate key : serviceAValidationFileBPerimeter")

  Scenario: Create perimeter for Service A Card Creation

    * def serviceACardCreationPerimeter =
"""
{
  "id" : "serviceACardCreationPerimeter",
  "process" : "servicea_cardcreation",
  "stateRights" : [
    {
      "state" : "freeMessageState",
      "right" : "ReceiveAndWrite"
    }
  ]
}
"""

    Given url opfabUrl + 'users/perimeters'
    And header Authorization = 'Bearer ' + authToken
    And request serviceACardCreationPerimeter
    When method post
    Then assert responseStatus == 201 || (responseStatus == 400 && response.errors[0] == "Duplicate key : serviceACardCreationPerimeter")

  Scenario: Create perimeter for Service B Process Monitoring

    * def serviceBProcessMonitoringPerimeter =
"""
{
  "id" : "serviceBProcessMonitoringPerimeter",
  "process" : "serviceb_processmonitoring",
  "stateRights" : [
    {
      "state" : "processsuccessful",
      "right" : "ReceiveAndWrite"
    },
    {
      "state" : "processfailed",
      "right" : "ReceiveAndWrite"
    }
  ]
}
"""

    Given url opfabUrl + 'users/perimeters'
    And header Authorization = 'Bearer ' + authToken
    And request serviceBProcessMonitoringPerimeter
    When method post
    Then assert responseStatus == 201 || (responseStatus == 400 && response.errors[0] == "Duplicate key : serviceBProcessMonitoringPerimeter")

  Scenario: Create perimeter for Service B Validation File A

    * def serviceBValidationFileAPerimeter =
"""
{
  "id" : "serviceBValidationFileAPerimeter",
  "process" : "serviceb_validation_filea",
  "stateRights" : [
    {
      "state" : "ok",
      "right" : "ReceiveAndWrite"
    },
    {
      "state" : "warning",
      "right" : "ReceiveAndWrite"
    },
    {
      "state" : "error",
      "right" : "ReceiveAndWrite"
    }
  ]
}
"""

    Given url opfabUrl + 'users/perimeters'
    And header Authorization = 'Bearer ' + authToken
    And request serviceBValidationFileAPerimeter
    When method post
    Then assert responseStatus == 201 || (responseStatus == 400 && response.errors[0] == "Duplicate key : serviceBValidationFileAPerimeter")

  Scenario: Create perimeter for Service B Validation File B

    * def serviceBValidationFileBPerimeter =
"""
{
  "id" : "serviceBValidationFileBPerimeter",
  "process" : "serviceb_validation_fileb",
  "stateRights" : [
    {
      "state" : "ok",
      "right" : "ReceiveAndWrite"
    },
    {
      "state" : "warning",
      "right" : "ReceiveAndWrite"
    },
    {
      "state" : "error",
      "right" : "ReceiveAndWrite"
    }
  ]
}
"""

    Given url opfabUrl + 'users/perimeters'
    And header Authorization = 'Bearer ' + authToken
    And request serviceBValidationFileBPerimeter
    When method post
    Then assert responseStatus == 201 || (responseStatus == 400 && response.errors[0] == "Duplicate key : serviceBValidationFileBPerimeter")

  Scenario: Create perimeter for Service B Card Creation

    * def serviceBCardCreationPerimeter =
"""
{
  "id" : "serviceBCardCreationPerimeter",
  "process" : "serviceb_cardcreation",
  "stateRights" : [
    {
      "state" : "freeMessageState",
      "right" : "ReceiveAndWrite"
    }
  ]
}
"""

    Given url opfabUrl + 'users/perimeters'
    And header Authorization = 'Bearer ' + authToken
    And request serviceBCardCreationPerimeter
    When method post
    Then assert responseStatus == 201 || (responseStatus == 400 && response.errors[0] == "Duplicate key : serviceBCardCreationPerimeter")

  Scenario: Create perimeter for Service A Smart Notification

    * def serviceACoordinationAPerimeter =
"""
{
  "id" : "serviceACoordinationAPerimeter",
  "process" : "servicea_coordinationa",
  "stateRights" : [
    {
      "state" : "initial",
      "right" : "ReceiveAndWrite"
    },
    {
      "state" : "answerProposalConfirmed",
      "right" : "ReceiveAndWrite"
    },
    {
      "state" : "answerProposalRejected",
      "right" : "ReceiveAndWrite"
    },
    {
      "state" : "answerDifferentChoices",
      "right" : "ReceiveAndWrite"
    },
    {
      "state" : "proposalConfirmed",
      "right" : "Receive"
    },
    {
      "state" : "proposalRejected",
      "right" : "Receive"
    },
    {
      "state" : "noAnswerProvided",
      "right" : "Receive"
    },
    {
      "state" : "differentChoices",
      "right" : "Receive"
    }
  ]
}
"""

    Given url opfabUrl + 'users/perimeters'
    And header Authorization = 'Bearer ' + authToken
    And request serviceACoordinationAPerimeter
    When method post
    Then assert responseStatus == 201 || (responseStatus == 400 && response.errors[0] == "Duplicate key : serviceACoordinationAPerimeter")

  Scenario: Add perimeters to group 'servicea'

    * def group = 'servicea'

    Given url opfabUrl + 'users/groups/' + group + '/perimeters'
    And header Authorization = 'Bearer ' + authToken
    And request ["serviceAProcessMonitoringPerimeter", "serviceAValidationFileAPerimeter", "serviceAValidationFileBPerimeter", "serviceACardCreationPerimeter", "serviceACoordinationAPerimeter"]
    When method patch
    Then status 200

  Scenario: Add perimeters to group 'serviceb'

    * def group = 'serviceb'

    Given url opfabUrl + 'users/groups/' + group + '/perimeters'
    And header Authorization = 'Bearer ' + authToken
    And request ["serviceBProcessMonitoringPerimeter", "serviceBValidationFileAPerimeter", "serviceBValidationFileBPerimeter", "serviceBCardCreationPerimeter"]
    When method patch
    Then status 200

#################################################
#               USER.TEST (RTE)                 #
#################################################

  Scenario: Create user user.test

    * def user =
"""
{
   "login" : "user.test",
   "firstName" : "User",
   "lastName" : "Test"
}
"""
  
    Given url opfabUrl + 'users/users'
    And header Authorization = 'Bearer ' + authToken
    And request user
    When method post
    Then assert responseStatus == 200 || responseStatus == 201
    And match response.login == user.login
    And match response.firstName == user.firstName
    And match response.lastName == user.lastName

  Scenario: Add user user.test to group Service A

    * def group = 'servicea'
    * def usersArray =
"""
[ "user.test" ]
"""

    Given url opfabUrl + 'users/groups/' + group + '/users'
    And header Authorization = 'Bearer ' + authToken
    And request usersArray
    When method patch
    And status 200

  Scenario: Add user user.test to group Service B

    * def group = 'serviceb'
    * def usersArray =
"""
[ "user.test" ]
"""

    Given url opfabUrl + 'users/groups/' + group + '/users'
    And header Authorization = 'Bearer ' + authToken
    And request usersArray
    When method patch
    And status 200

  Scenario: Add user user.test to entity RTE

    * def entity = '10XFR-RTE------Q'
    * def usersArray =
"""
[ "user.test" ]
"""

    Given url opfabUrl + 'users/entities/' + entity + '/users'
    And header Authorization = 'Bearer ' + authToken
    And request usersArray
    When method patch
    And status 200

  Scenario: Patch user settings with user.test

    * def userSettings =
"""
{
  "login" : "user.test",
  "description" : "RTE",
  "timeZone" : "Europe/Paris",
  "locale" : "en"
}
"""

    Given url opfabUrl + 'users/users/' + userSettings.login + '/settings'
    And header Authorization = 'Bearer ' + authToken
    And request userSettings
    When method patch
    Then print response
    And status 200
    And match response.login == userSettings.login
    And match response.description == userSettings.description
    And match response.timeZone == userSettings.timeZone
    And match response.locale == userSettings.locale
    # And match response.timeFormat == userSettings.timeFormat
    # And match response.dateFormat == userSettings.dateFormat
    # And match response.defaultTags == userSettings.defaultTags

#################################################
#               USER.TEST2 (TERNA)              #
#################################################

  Scenario: Create user user.test2

    * def user =
"""
{
   "login" : "user.test2",
   "firstName" : "User",
   "lastName" : "Test 2"
}
"""

    Given url opfabUrl + 'users/users'
    And header Authorization = 'Bearer ' + authToken
    And request user
    When method post
    Then assert responseStatus == 200 || responseStatus == 201
    And match response.login == user.login
    And match response.firstName == user.firstName
    And match response.lastName == user.lastName

  Scenario: Add user user.test2 to group Service A

    * def group = 'servicea'
    * def usersArray =
"""
[ "user.test2" ]
"""

    Given url opfabUrl + 'users/groups/' + group + '/users'
    And header Authorization = 'Bearer ' + authToken
    And request usersArray
    When method patch
    And status 200

  Scenario: Add user user.test2 to entity Terna

    * def entity = '10X1001A1001A345'
    * def usersArray =
"""
[ "user.test2" ]
"""

    Given url opfabUrl + 'users/entities/' + entity + '/users'
    And header Authorization = 'Bearer ' + authToken
    And request usersArray
    When method patch
    And status 200

  Scenario: Patch user settings with user.test2

    * def userSettings =
"""
{
  "login" : "user.test2",
  "description" : "Terna",
  "timeZone" : "Europe/Paris",
  "locale" : "en"
}
"""

    Given url opfabUrl + 'users/users/' + userSettings.login + '/settings'
    And header Authorization = 'Bearer ' + authToken
    And request userSettings
    When method patch
    Then print response
    And status 200
    And match response.login == userSettings.login
    And match response.description == userSettings.description
    And match response.timeZone == userSettings.timeZone
    And match response.locale == userSettings.locale
    # And match response.timeFormat == userSettings.timeFormat
    # And match response.dateFormat == userSettings.dateFormat
    # And match response.defaultTags == userSettings.defaultTags


#################################################
#                   USER.RTE                    #
#################################################

  Scenario: Create user user.rte

    * def user =
"""
{
   "login" : "user.rte"
}
"""

    Given url opfabUrl + 'users/users'
    And header Authorization = 'Bearer ' + authToken
    And request user
    When method post
    Then assert responseStatus == 200 || responseStatus == 201
    And match response.login == user.login

  Scenario: Add user user.rte to group Service A

    * def group = 'servicea'
    * def usersArray =
"""
[ "user.rte" ]
"""

    Given url opfabUrl + 'users/groups/' + group + '/users'
    And header Authorization = 'Bearer ' + authToken
    And request usersArray
    When method patch
    And status 200

  Scenario: Add user user.rte to group Service B

    * def group = 'serviceb'
    * def usersArray =
"""
[ "user.rte" ]
"""

    Given url opfabUrl + 'users/groups/' + group + '/users'
    And header Authorization = 'Bearer ' + authToken
    And request usersArray
    When method patch
    And status 200

  Scenario: Add user user.rte to entity RTE

    * def entity = '10XFR-RTE------Q'
    * def usersArray =
"""
[ "user.rte" ]
"""

    Given url opfabUrl + 'users/entities/' + entity + '/users'
    And header Authorization = 'Bearer ' + authToken
    And request usersArray
    When method patch
    And status 200

  Scenario: Patch user settings with user.rte

    * def userSettings =
"""
{
  "login" : "user.rte",
  "timeZone" : "Europe/Paris",
  "locale" : "en"
}
"""

    Given url opfabUrl + 'users/users/' + userSettings.login + '/settings'
    And header Authorization = 'Bearer ' + authToken
    And request userSettings
    When method patch
    Then print response
    And status 200
    And match response.login == userSettings.login
    And match response.timeZone == userSettings.timeZone
    And match response.locale == userSettings.locale
    # And match response.timeFormat == userSettings.timeFormat
    # And match response.dateFormat == userSettings.dateFormat
    # And match response.defaultTags == userSettings.defaultTags


#################################################
#                  USER.TERNA                   #
#################################################

  Scenario: Create user user.terna

    * def user =
"""
{
   "login" : "user.terna"
}
"""

    Given url opfabUrl + 'users/users'
    And header Authorization = 'Bearer ' + authToken
    And request user
    When method post
    Then assert responseStatus == 200 || responseStatus == 201
    And match response.login == user.login

  Scenario: Add user user.terna to group Service A

    * def group = 'servicea'
    * def usersArray =
"""
[ "user.terna" ]
"""

    Given url opfabUrl + 'users/groups/' + group + '/users'
    And header Authorization = 'Bearer ' + authToken
    And request usersArray
    When method patch
    And status 200

  Scenario: Add user user.terna to group Service B

    * def group = 'serviceb'
    * def usersArray =
"""
[ "user.terna" ]
"""

    Given url opfabUrl + 'users/groups/' + group + '/users'
    And header Authorization = 'Bearer ' + authToken
    And request usersArray
    When method patch
    And status 200

  Scenario: Add user user.terna to entity Terna

    * def entity = '10X1001A1001A345'
    * def usersArray =
"""
[ "user.terna" ]
"""

    Given url opfabUrl + 'users/entities/' + entity + '/users'
    And header Authorization = 'Bearer ' + authToken
    And request usersArray
    When method patch
    And status 200

  Scenario: Patch user settings with user.terna

    * def userSettings =
"""
{
  "login" : "user.terna",
  "timeZone" : "Europe/Paris",
  "locale" : "en"
}
"""

    Given url opfabUrl + 'users/users/' + userSettings.login + '/settings'
    And header Authorization = 'Bearer ' + authToken
    And request userSettings
    When method patch
    Then print response
    And status 200
    And match response.login == userSettings.login
    And match response.timeZone == userSettings.timeZone
    And match response.locale == userSettings.locale
    # And match response.timeFormat == userSettings.timeFormat
    # And match response.dateFormat == userSettings.dateFormat
    # And match response.defaultTags == userSettings.defaultTags


#################################################
#                 USER.AMPRION                  #
#################################################

  Scenario: Create user user.amprion

    * def user =
"""
{
   "login" : "user.amprion"
}
"""

    Given url opfabUrl + 'users/users'
    And header Authorization = 'Bearer ' + authToken
    And request user
    When method post
    Then assert responseStatus == 200 || responseStatus == 201
    And match response.login == user.login

  Scenario: Add user user.amprion to group Service A

    * def group = 'servicea'
    * def usersArray =
"""
[ "user.amprion" ]
"""

    Given url opfabUrl + 'users/groups/' + group + '/users'
    And header Authorization = 'Bearer ' + authToken
    And request usersArray
    When method patch
    And status 200

  Scenario: Add user user.amprion to group Service B

    * def group = 'serviceb'
    * def usersArray =
"""
[ "user.amprion" ]
"""

    Given url opfabUrl + 'users/groups/' + group + '/users'
    And header Authorization = 'Bearer ' + authToken
    And request usersArray
    When method patch
    And status 200

  Scenario: Add user user.amprion to entity Amprion

    * def entity = '10XDE-RWENET---W'
    * def usersArray =
"""
[ "user.amprion" ]
"""

    Given url opfabUrl + 'users/entities/' + entity + '/users'
    And header Authorization = 'Bearer ' + authToken
    And request usersArray
    When method patch
    And status 200

  Scenario: Patch user settings with user.amprion

    * def userSettings =
"""
{
  "login" : "user.amprion",
  "timeZone" : "Europe/Paris",
  "locale" : "en"
}
"""

    Given url opfabUrl + 'users/users/' + userSettings.login + '/settings'
    And header Authorization = 'Bearer ' + authToken
    And request userSettings
    When method patch
    Then print response
    And status 200
    And match response.login == userSettings.login
    And match response.timeZone == userSettings.timeZone
    And match response.locale == userSettings.locale
    # And match response.timeFormat == userSettings.timeFormat
    # And match response.dateFormat == userSettings.dateFormat
    # And match response.defaultTags == userSettings.defaultTags


#################################################
#                 USER.CORESO                   #
#################################################

  Scenario: Create user user.coreso

    * def user =
"""
{
   "login" : "user.coreso"
}
"""

    Given url opfabUrl + 'users/users'
    And header Authorization = 'Bearer ' + authToken
    And request user
    When method post
    Then assert responseStatus == 200 || responseStatus == 201
    And match response.login == user.login

  Scenario: Add user user.coreso to group Service A

    * def group = 'servicea'
    * def usersArray =
"""
[ "user.coreso" ]
"""

    Given url opfabUrl + 'users/groups/' + group + '/users'
    And header Authorization = 'Bearer ' + authToken
    And request usersArray
    When method patch
    And status 200

  Scenario: Add user user.coreso to group Service B

    * def group = 'serviceb'
    * def usersArray =
"""
[ "user.coreso" ]
"""

    Given url opfabUrl + 'users/groups/' + group + '/users'
    And header Authorization = 'Bearer ' + authToken
    And request usersArray
    When method patch
    And status 200

  Scenario: Add user user.coreso to entity CORESO

    * def entity = '22XCORESO------S'
    * def usersArray =
"""
[ "user.coreso" ]
"""

    Given url opfabUrl + 'users/entities/' + entity + '/users'
    And header Authorization = 'Bearer ' + authToken
    And request usersArray
    When method patch
    And status 200

  Scenario: Patch user settings with user.coreso

    * def userSettings =
"""
{
  "login" : "user.coreso",
  "timeZone" : "Europe/Paris",
  "locale" : "en"
}
"""

    Given url opfabUrl + 'users/users/' + userSettings.login + '/settings'
    And header Authorization = 'Bearer ' + authToken
    And request userSettings
    When method patch
    Then print response
    And status 200
    And match response.login == userSettings.login
    And match response.timeZone == userSettings.timeZone
    And match response.locale == userSettings.locale
    # And match response.timeFormat == userSettings.timeFormat
    # And match response.dateFormat == userSettings.dateFormat
    # And match response.defaultTags == userSettings.defaultTags


#################################################
#                    CHECK!!!                   #
#################################################


  Scenario: Get current user (user.test2) with perimeters

    * def signInUserTest = call read('./getToken.feature') { username: 'user.test2'}

    Given url opfabUrl + 'users/CurrentUserWithPerimeters'
    And header Authorization = 'Bearer ' + signInUserTest.authToken
    When method get
    Then status 200
    And match response.userData.login == 'user.test2'
    And assert response.computedPerimeters.length == 17

  Scenario: Get current user (user.test) with perimeters

    * def signInUserTest = call read('./getToken.feature') { username: 'user.test'}

    Given url opfabUrl + 'users/CurrentUserWithPerimeters'
    And header Authorization = 'Bearer ' + signInUserTest.authToken
    When method get
    Then status 200
    And match response.userData.login == 'user.test'
    And assert response.computedPerimeters.length == 26