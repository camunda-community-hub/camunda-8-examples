# Tasklist synchronized by Task listener or polling

## What does this example do?

This example shows how a user task synchronization mechanism could look like for Camunda if both - synchronization via task listener AND polling is required.

Before Camunda 8.9 will be shipped with global task listeners, both mechanisms are expected.

## How does it work?

The `TaskService` contains the whole logic required to sync user tasks.

* a job worker that is configured to react on lifecycle changes of the user task
* a scheduled polling that pages through the user task search API

To prevent interferences and race conditions, a user task that has ever been touched by a listener will never be updated by polling.

## How can I try this out?

Configure the camunda spring boot starter to connect to your orchestration cluster.

Then, run the application using

```shell
mvn spring-boot:run
```