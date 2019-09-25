@new
Feature: Add a new kind of measurement
  In order to have source information to examine later
  I want to be able to capture a measurement of several metrics at a specific time

  Scenario: Add a new measurement with valid (numeric) values
    # POST /measurements
    When I submit a new measurement as follows:
      | timestamp                  | temperature | dewPoint | precipitation | cloudCover |
      | "2015-09-01T16:00:00.000Z" | 27.1        | 16.7     | 0             | 14         |
    Then the response has a status code of 201
    And the Location header has the path "/measurements/2015-09-01T16:00:00.000Z"
