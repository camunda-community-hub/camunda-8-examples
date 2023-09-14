# Async Service Task Example

## What is the use case?

The implementation of a service task might be asynchronous. Usually, you would handle this using the Messaging Pattern (Send - Receive).

But there might be cases when the requirement to the process is to hide this kind of implementation detail and instead represent the service call as one task.

In this case, the implementation needs to be able to cover this kind of call by sending a request in an idempotent way and then waiting for the answer.

## How does it work?

This implementation will check for a transactionId, create an transactionId on service task scope if missing and uses it to send a request to a service and then check for the answer.

Then, it checks for the answer being present.

If not, it defers the polling by failing the job while leaving the amount of retries untouched.

## What are the constraints of the implementation?

The constraints of the implementation lie in the `AsyncService`. Here, the assumption is that creation of a transaction requires an ID.

As soon as complete, the result can be fetched over and over until it is completed (which will remove the transaction from the `AsyncService`).

## How can I try it out?

Configure the zeebe connection in the `application.yaml`. By default, it points to a plaintext local zeebe gateway.

Then, start the app by running

```shell
mvn spring-boot:run
```

On starting up, the example process is deployed.

After the app is up and running, a process instance can be started with a  `POST` to `http://localhost:8080/start`.

The process instance executes one service task that has an asynchronous implementation. In the logs, you should be able to see that the `AsyncService` is triggered from time to time, only creating a transaction on the first invocation.

Then, the result is fetched. After the result has been fetched, the next poll will return it and the job is completed.
