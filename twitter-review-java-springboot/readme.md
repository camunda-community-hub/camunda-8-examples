# Camunda 8 Spring Boot Process Solution Example

The self-contained process solution contains

* The process model as BPMN (auto-deployed during startup)
* Glue code for the service task
* REST endpoint that then starts a process instance
* Test case

Requirements:

* Camunda Platform 8
* Java >= 17
* Maven

## How to run

* Download/clone the code in this folder.
* Create a Camunda 8 SaaS cluster and add API client connection details in the file `application.properties`. Simply replace the existing sample values.
* Run the application:

```
mvn package exec:java
```

```
curl -i -X PUT http://localhost:8080/tweet
```
