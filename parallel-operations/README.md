# Parallel Operations

This is an example that shows how Camunda 7 and 8 can run in parallel.

Here, the embedded Camunda 7 engine setup is used to demonstrate interoperability between them.

## Important aspects

Camunda 7 and 8 share the same "Delegates", the methods that are defined in `MyDelegates`.

They can then be invoked from Camunda 7 as expression (`${myDelegates.sharedTask(someText).someOtherText()}` with result variable `someOtherText`) or from Camunda 8 as job worker (`sharedTask`).

## Requirements

This example is built on the `camunda-bpm-spring-boot-starter` (plus webapp) and `spring-boot-starter-camunda`.

## Testing

The example contains a test that performs a unit test for the processes.

>Note: It is not easily possible to use the embedded zeebe engine as the versions of org.camunda.feel are not aligned between Camunda 7 and 8.

## Running the example

### Explanation

By default, the `application.yaml` is configured to connect to a running camunda 8 instance. You can learn [here](https://docs.camunda.io/docs/self-managed/quickstart/developer-quickstart/docker-compose/) how to set it up.

Camunda 7 will start with an in-memory h2 database. To adjust this, you need to configure the spring boot datasource.

### Steps

Follow the docs to get Camunda 8 up and running.

Now, go back to this directly and run this example:

```shell
cd <location of this project>
mvn spring-boot:run
```

Now, you can start process instances by sending:

```shell
POST http://localhost:8080/process
```

You can then go to [Camunda 7 cockpit](http://localhost:8080/camunda) and [Camunda 8 Operate](http://localhost:8081) and inspect the run process instances.
