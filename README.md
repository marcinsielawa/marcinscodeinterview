# Application request manager

Developed using Spring boot, Java 25.

## Stage I. Initialization

Acceptence criteria: app is healthy

I start off by creating an empty spring boot project with some useful dependencies.
We will be using h2 in memory database and standard non-reactive web.

`
spring init --dependencies=lombok,web,h2,data-jpa,actuator --build=maven inverview -a applicationrequestmanager -g com.marcinsielawa -j 25
cd inverview
`
`
mvn spring-boot:run
`

`
curl 127.0.0.1:8080/actuator/health
`
`
{"groups":["liveness","readiness"],"status":"UP"}
`

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


