package io.camunda.example.process;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.DeploymentEvent;
import io.camunda.example.ClientProvider;
import io.camunda.example.ClientProvider.AuthMethod;

/**
 * Example application that connects to a cluster on Camunda Cloud, or a locally deployed cluster.
 *
 * <p>It uses {@link ClientProvider} to create a client with the appropriate authentication method.
 */
public final class ProcessDeployer {

  public static void main(final String[] args) {
    try (final CamundaClient client = ClientProvider.createCamundaClient(AuthMethod.none)) {

      final DeploymentEvent deploymentEvent =
          client.newDeployResourceCommand().addResourceFromClasspath("demoProcess.bpmn").execute();

      System.out.println("Deployment created with key: " + deploymentEvent.getKey());
    }
  }
}
