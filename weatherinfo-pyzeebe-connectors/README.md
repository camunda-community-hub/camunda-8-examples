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

To run the project, you have to add your personal configuration:
1. Replace the Camunda SaaS credentials in `pyzeebeWorker.py` to match your cluster.
2. Add API keys for Openweathermap and Send Grid as secrets to your cluster. Read more about it in the [documentation](https://docs.camunda.io/docs/components/console/manage-clusters/manage-secrets/)
  * OWM_API_KEY holds the API key for Openweathermap
  * SEND_GRID_API_KEY holds the API key for Send Grid