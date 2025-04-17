# Secret Provider as Credentials Provider

Currently, the Camunda Connectors offer various ways to apply credentials to a request.

Especially OAuth requires extensive configuration.

## What is the example about?

This example shows how a secret provider can be implemented that allows for fetching a token without configuring this in the connector itself.

## Why do I need this?

>Especially OAuth requires extensive configuration.

One main motivation here is to allow for abstraction of complex constructs like building a complex authentication.

Further ideas could be to implement similar secret providers that allow for maintaining a registry of connection details.

## How does it work?

### Run the platform in identity mode

The example is configured to run with a local docker-compose of camunda-platform.

To start, you have to first run camunda in [docker-compose](https://docs.camunda.io/docs/self-managed/setup/deploy/local/docker-compose/#run-camunda-8-with-docker-compose). Please do *not* use the `-core` variant as we need Identity + Keycloak.

Also, you need to set `ZEEBE_AUTHENTICATION_MODE=identity` in the `.env` file.

### Start the application

After Camunda is running in docker-compose, you can start the application:

```shell
mvn spring-boot:run
```

### Run the test process

To run the test process, deploy the process from `src/test/resources/test.bpmn` to your Camunda instance using the Camunda Modeler and run an instance.

If you inspect the process instance in Operate, you should find the topology of the zeebe cluster as a variable.

This is the response coming from the REST connector call that was used.
