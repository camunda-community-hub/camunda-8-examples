[![](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)

# Camunda 8 Examples

A collection of examples related to the usage of Camunda 8.

## Table of Contents

1. **[Async Service Task Example](async-service-task/README.md)** : Implementation of an asynchronous service task with handling for an asynchronous service call.

2. **[Multi-instance Processing of a (Very) Large Sequence](large-multi-instance-example/README.md)** : Example demonstrating the processing of a very large sequence of entities using multi-instance subprocesses.

3. **[Payment Example Process Application](payment-example-process-application/README.md)** : Example of a process application containing a payment process and the required workers, forms, and dummy services.
   - **Deployment Example on Kubernetes**: [Available](payment-example-process-application/kube/README.md)

4. **[Integrate Camunda 8 with Microsoft PowerApps Dataverse](powerapps-dataverse/README.md)** : Guide for connecting the Camunda 8 REST Connector to Microsoft PowerApps Dataverse via OAuth2 to query data from tables.

5. **[Rollback on Error Example](rollback-on-error-example/README.md)** : Example of rollback on error in a process where two actions need to be executed synchronously.

6. **[Example for synchronous responses from processes](synchronous-response-springboot/README.md)** : Example of synchronous response from Camunda 8 processes.

7. **[Timer Testing](timer-testing/README.md)** : Example demonstrating how to test timers in Zeebe.

8. **[Pyzeebe & Connectors Example](weatherinfo-pyzeebe-connectors/README.md)** : Project containing a process, user form, Python worker using the pyzeebe client, and a custom worker template.

9. **[Zeebe Java Client Examples](zeebe-client-plain-java/README.md)** : Maven project containing several examples using the Zeebe Java client.

## How to use

Check out the repo. Navigate to the project you are interested in. Each project should contain:

* an introduction to the purpose
* a brief description of the functionality
* a guide on how to setup and test the example

## Report problems

If problems occur, please file an issue containing:

* which project should be used
* what did not work out
* which environment was used (java version, build tool, ...)
