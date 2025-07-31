package io.camunda.example.process;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.example.ClientProvider;
import io.camunda.example.ClientProvider.AuthMethod;

/**
 * Example application that connects to a cluster on Camunda Cloud, or a locally deployed cluster.
 *
 * <p>It uses {@link ClientProvider} to create a client with the appropriate authentication method.
 */
public final class ProcessInstanceCreator {

  public static void main(final String[] args) {
    final String bpmnProcessId = "demoProcess";

    try (final CamundaClient client = ClientProvider.createCamundaClient(AuthMethod.none)) {

      System.out.println("Creating process instance");

      final ProcessInstanceEvent processInstanceEvent =
          client.newCreateInstanceCommand().bpmnProcessId(bpmnProcessId).latestVersion().execute();

      System.out.println(
          "Process instance created with key: " + processInstanceEvent.getProcessInstanceKey());
    }
  }
}
