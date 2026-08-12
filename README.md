# Application request manager

Developed using Spring boot, Java 25.

Stage I. Initialization

I start off by creating an empty spring boot project with some useful  dependencies.
We will be using h2 in memory database and standard non-reactive web.

`
spring init --dependencies=lombok,web,h2,data-jpa,actuator --build=maven inverview -a applicationrequestmanager -g com.marcinsielawa -j 25
cd inverview
mvn spring-boot:run
`

Acceptence criteria: healcheck returns healthy

`
curl 127.0.0.1:8080/actuator/health
{"groups":["liveness","readiness"],"status":"UP"}
`
