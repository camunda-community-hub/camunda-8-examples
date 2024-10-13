# Instance Migration Workaround for Camunda 8

Currently, Camunda 8 does not support instance migration.
This prototype shows how we can use the Zeebe and Operate API to move process instances from one model version to another:
1. The process instances must have entered a wait state, such as a user task or a service task for which the external task worker has been deactivated.
2. For each instance of the old version, an instance in the new version is created.
   * Variables are copied
   * The instance is modified to start at a predefined point (i.e., the wait state)
3. The old instance is canceled.

This prototype considers the two latest versions of a model. Older versions are ignored.
This prototype is heavily inspired by the [Camunda 7 to 8 migration tooling](https://github.com/camunda-community-hub/camunda-7-to-8-migration).
## Configuration

Everything is preconfigured for local testing (i.e., localhost addresses without encryption).
You can configure the connection to Zeebe as described [here](https://github.com/camunda-community-hub/spring-zeebe).
You can furthermore use the following properties to configure the connection to Operate:
```
operate:
  url: https://bru-2.operate.camunda.io/757dbc30-5127-4bed-XXXX-XXXXXXXXXXXX
  # for self-managed setup configure
  keycloak:
    realm: camunda-platform
    url: https://mykeycloak.example.com
```

## Version Information

This prototype has been build for Camunda 8.1.8.
It is compatible with both self-managed and SaaS deployments.

Camunda 8.2 provides additional API endpoints, which can be used to improve this prototype:
We can get the element ID of currently enabled flow nodes.
With this information, activity "wait until all processes reached waiting state" becomes obsolete.