# Spring Connector

This example shows how a connector can be implemented as Spring Bean and be picked up from the Spring bean context instead of the SPI.

## Details

The connector implementation itself uses an injected bean (which is not possible from SPI).

To make the connector available via Spring bean context, it is configured in an `@AutoConfiguration` which is registered as `spring.factories`.

From here, it will be picked up during auto-configuration and be processed by connectors-spring.

## Runtimes

There are 2 possible runtimes presented.

### Injection to existing connector bundle

After building the project with maven, run

```shell
docker-compose up -d
```

from the `./docker` directory.

This will start the normal connectors-bundle and mount the connector jar to a location inside the container from where it will be added to the classpath.

### Build your own runtime

Inside `./example-connector-runtime`, you will find a simple Spring Boot project that allows to run the connector as simple dependency.

Just run

```shell
mvn spring-boot:run
```