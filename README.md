# Application request manager

Developed using Spring boot, Java 25.

## Stage I. Initialization

Acceptence criteria: app is healthy

I start off by creating an empty spring boot project with some useful dependencies.
We will be using h2 in memory database and standard non-reactive web.

`spring init --dependencies=lombok,web,h2,data-jpa,actuator --build=maven inverview -a applicationrequestmanager -g com.marcinsielawa -j 25
cd inverview`

`mvn spring-boot:run`

`curl 127.0.0.1:8080/actuator/health`
`{"groups":["liveness","readiness"],"status":"UP"}`

## Stage II. Create application use case

Acceptence criteria: a created application is stored in the database

### Part 1

We benefit form the fact that the state transitions and entity properties are specified beforehand.
I chose to generate the REST controller and DTOs using the open api swagger maven plugin.
I needed to add some spring doc and jackson libraries to make it work as expected.
After the code compiled I created a stub controller and a test to see the basic input validations take place.

### Part 2
Its time to implement the Create Application. Since we know the state names and transitions, we can implement the
transitions as a compile-time safe state machine. 
I introduce a stub service bean, a test to drive the use case development, records and intefaces that make up the service business interface.

### Part 3
I want the create use case to write a new entry in the main store and publish a domain event for the audit log 
For this to work I will implement the service test using a mock repository bean, mock appliction event publisher.
I will acompany them by a jpa data test of the h2 backed CRUD repository and I will model database entity for the Application
So far we will only persist the application, in the next I will implement event triggering. 
Below one can see an example create and retrieval of an application

`
curl -v -X POST http://127.0.0.1:8080/api/applications \
-H 'Content-Type: application/json' \
-d '{
  "name": "cash",
  "body": "gas"
}'                 
...
Location: /api/applications/045ddbb2-3897-41f9-9b07-2fce79f7f8a5
`

`
 curl 127.0.0.1:8080/api/applications/117199f8-1db9-49cc-8666-96cadf5c4638 
{"id":"117199f8-1db9-49cc-8666-96cadf5c4638","state":"CREATED","name":"gas","body":"cash","createdAt":"2026-08-13T10:25:49.909364+02:00"}
`
### PART 4

The audit logger has been implemented as a spring application event listener. It stores the domain event during the same transaction as writing to the application store and its synchronous.
