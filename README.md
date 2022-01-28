# lets talk about weather
## description
Spring boot HTTP application that stores (transient, in-memory) weather measurements (temperature, dew point, and precipation) at specific UTC datetimes and returns measurements based on the provided data

## endpoints
### 1. `POST /localhost:8080/measurements`
adds weather measurements (temperature, dew point, and precipation at a specific time) to the application in order to have source information to examine later
##### request body format:
```
| timestamp                  | temperature | dewPoint | precipitation |
| "2015-09-01T16:00:00.000Z" | 27.1        | 16.7     | 0             |
| "2015-09-01T16:10:00.000Z" | 27.3        | 16.9     | 0             |
| "2015-09-01T16:20:00.000Z" | 27.5        | 17.1     | 0             |
| "2015-09-01T16:30:00.000Z" | 27.4        | 17.3     | 0             |
| "2015-09-01T16:40:00.000Z" | 27.2        | 17.2     | 0             |
| "2015-09-02T16:00:00.000Z" | 28.1        | 18.3     | 0             |
```
##### http response status codes
- `201`: the measurement data has been stored successsfully
- `400`: failure due to missing/invalid temperature, dew point, and/or precipitation values
- `401`: failure due to missing/invalid timestamp

### 2. `GET /measurements/{timestamp}` retrieves weather measurements at the specified timestamp
##### URL example: 
`GET /measurements/2015-09-01T16:20:00.000Z`
##### http responses
- `200`: the measurement data is present for the specificed timestamp and is returned to the user in the response body:
```
| timestamp                  | temperature | dewPoint | precipitation |
| "2015-09-01T16:20:00.000Z" | 27.5        | 17.1     | 0             |
```
- `404`: the measurement data does not exist for the specified timestamp

## dependencies
- Java 8
- Maven
- NodeJS 8


## commands
run the app:
`mvn spring-boot:run -e`

compile and integration test [using predefined test data](./.c1/assets/c1-code-test-take-home-tester-1.1.1.tgz):
`mvn clean verify`

- test output is written to both `stdout` and `integration-test.log`


