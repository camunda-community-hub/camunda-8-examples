# User task micro frontend

This is an example on how a user task micro frontend can be built.

## What is the purpose?

To embed a Camunda task into your own tasklist, you might require a micro frontend for a composed web application.

## What does it cover?

The current example covers:

* backend integration between Tasklist/Camunda API and micro frontend backend
* creation of plain html/js task view
* handling of completed forms by making all components read-only
* handling of submit buttons by removing all of them from the original form and only showing one

## How do I set it up?

To start the application, configure the `application.yaml` so that:

* the tasklist client is configured according to your tasklist api (base url and authentication)
* task camunda client is configured according to your camunda api (addresses and authentication)

Then, you can run the application with:

```shell
mvn spring-boot:run
```