package io.camunda.example.process;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.CamundaFuture;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.example.ClientProvider;
import io.camunda.example.ClientProvider.AuthMethod;

/**
 * Example application that connects to a cluster on Camunda Cloud, or a locally deployed cluster.
 *
 * <p>It uses {@link ClientProvider} to create a client with the appropriate authentication method.
 */
public final class NonBlockingProcessInstanceCreator {
  public static void main(final String[] args) {
    final int numberOfInstances = 100_000;
    final String bpmnProcessId = "demoProcess";

    try (final CamundaClient client = ClientProvider.createCamundaClient(AuthMethod.none)) {
      System.out.println("Creating " + numberOfInstances + " process instances");

      final long startTime = System.currentTimeMillis();

      long instancesCreating = 0;

      while (instancesCreating < numberOfInstances) {
        // this is non-blocking/async => returns a future
        final CamundaFuture<ProcessInstanceEvent> future =
            client.newCreateInstanceCommand().bpmnProcessId(bpmnProcessId).latestVersion().send();

        // could put the future somewhere and eventually wait for its completion

        instancesCreating++;
      }

      // creating one more instance; joining on this future ensures
      // that all the other create commands were handled
      client.newCreateInstanceCommand().bpmnProcessId(bpmnProcessId).latestVersion().execute();

      System.out.println("Took: " + (System.currentTimeMillis() - startTime));
    }
  }
}
