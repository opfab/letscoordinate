# Copyright (c) 2020, RTE (https://www.rte-france.com)
# Copyright (c) 2020 RTE international (https://www.rte-international.com)
# See AUTHORS.txt
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# SPDX-License-Identifier: MPL-2.0
# This file is part of the Let’s Coordinate project.


Feature: Prepare OpFab env. for Let's Co open source

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

  Scenario: Add user user.test to entity Terna

    * def entity = '10X1001A1001A345'
    * def usersArray =
"""
[ "user.test" ]
"""

    Given url opfabUrl + 'users/entities/' + entity + '/users'
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
  "description" : "TSO A",
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


  Scenario: Create perimeter for Service A

    * def serviceAPerimeter =
"""
{
  "id" : "serviceAPerimeter",
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
    And request serviceAPerimeter
    When method post
    Then status 201


  Scenario: Create perimeter for Service B

    * def serviceBPerimeter =
"""
{
  "id" : "serviceBPerimeter",
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
    And request serviceBPerimeter
    When method post
    Then status 201

  Scenario: Add serviceAPerimeter for group 'servicea'

    * def group = 'servicea'

    Given url opfabUrl + 'users/groups/' + group + '/perimeters'
    And header Authorization = 'Bearer ' + authToken
    And request ["serviceAPerimeter"]
    When method patch
    Then status 200


  Scenario: Add serviceAPerimeter for group 'serviceb'

    * def group = 'serviceb'

    Given url opfabUrl + 'users/groups/' + group + '/perimeters'
    And header Authorization = 'Bearer ' + authToken
    And request ["serviceBPerimeter"]
    When method patch
    Then status 200


  Scenario: Get current user (user.test) with perimeters

    * def signInUserTest = call read('./getToken.feature') { username: 'user.test'}

    Given url opfabUrl + 'users/CurrentUserWithPerimeters'
    And header Authorization = 'Bearer ' + signInUserTest.authToken
    When method get
    Then status 200
    And match response.userData.login == 'user.test'
    And assert response.computedPerimeters.length == 2

################################################################

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


  Scenario: Add user user.test2 to entity RTE

    * def entity = '10XFR-RTE------Q'
    * def usersArray =
"""
[ "user.test2" ]
"""

    Given url opfabUrl + 'users/entities/' + entity + '/users'
    And header Authorization = 'Bearer ' + authToken
    And request usersArray
    When method patch
    And status 200


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
