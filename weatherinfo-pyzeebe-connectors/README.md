# Pyzeebe & Connectors Example

This project contains
* a process
  * receiving user input
  * calling openweathermap via the REST connector
  * calling a worker for looking up a suited activity for the given weather (via custom template)
  * sending an email to the user via the Send Grid Connector
* a form for the user input
* a worker written in Python, using the [pyzeebe](https://github.com/camunda-community-hub/pyzeebe) client
* a custom template for the worker

This example can be used most easily in [Camunda 8 SaaS](https://console.cloud.camunda.io) with the WebModeler and Connectors provided.

Upload the process model to the web modeler and deploy it to your cluster. Process instances can be started from the Web Modeler.
