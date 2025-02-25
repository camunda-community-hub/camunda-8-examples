# Versioned Job Worker

This example shows how a non-backwards compatible job worker can be created using the process definition version tag.

## Setup

Run Camunda using our [docker compose setup](https://github.com/camunda/camunda-self-managed/tree/main/docker-compose).

Then, start the application:

```shell
mvn spring-boot:run
```

The application will deploy 2 processes, being the same but having 2 different version tags. As they are deployed at the same time, they also have 2 different process definition ids, but this does not matter for the example.

## Test the implementation

The process definition ids are:

* `ExampleV1Process`
* `ExampleV2Process`

You can start instances from them using the [Camunda Rest API](https://docs.camunda.io/docs/apis-tools/camunda-api-rest/specifications/create-process-instance/).

No other input that the process definition id is required.

After an instance is started, you should see a log for the according version in your application log, which means that the job (which has a single job handler) is correctly routed.

## Alternative solutions

If a job worker version should not be bound to the process definition version, you can also use custom headers to implement a router mechanism based on individual worker or task versions.
