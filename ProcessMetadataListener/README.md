# Process metadata listener

This example shows how a generic metadata listener for a process could look like.

## What does it do?

The example contains a job worker for the type `metadata` that set a variable `camunda`:

```json
{
  "processDefinitionId": "",
  "processDefinitionKey": "",
  "versionTag": "",
  "processInstanceKey": "",
  "version": 0
}
```

## Why do I need this?

Camunda can currently not provide context information about the process instance from the feel context. This execution listener fills the gap.

## How can I make use of it?

### As part of the connector runtime

You can build the project using maven:

```shell
mvn clean package
```

Then, you can copy the resulting jar file to your connector runtime under `/opt/custom`.

### As part of your own project

You can also copy/fork this project source code and use the worker in your own java project.
