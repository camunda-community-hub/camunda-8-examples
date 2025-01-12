# Extended Connector Runtime

As the connector runtime lies outside the engine, it can be adjusted.

This example shows how a custom feel function can be introduced.

This feel function can then be used on a connector runtime side feel expressions (for example result expression and error expression on outbound connectors).

## How this works

As the connector runtime uses a standardized way to bootstrap the feel engine and this way includes using the SPI function provider, this can be used to extend the functions being used.

The [SPI file](./src/main/resources/META-INF/services/org.camunda.feel.context.CustomFunctionProvider) points to the function provider that determines on how the function is loaded.

The provider then resolves the function by its name.

The function itself is instantiated and gets a static reference to a spring bean injected. This workaround is chosen as there is no way to access the SPI context from within spring.

## What kind of example is implemented here

This example adds a function:

```
nextExecutionBackoff(backoff: days-time-duration): days-time-duration
```

This function then uses the `schedulingService` bean to determine the next timestamp where the execution could happen according to a defined schedule.

## How can I run it

As the example comes as spring boot application containing the rest connector, you can configure the connection to camunda and start the application using

```bash
mvn spring-boot:run
```

>Tip: Disable the default connectors coming with your Camunda 8 installation to test out this runtime.
