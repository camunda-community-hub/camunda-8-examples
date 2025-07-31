package io.camunda.example.process;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceResult;
import io.camunda.example.ClientProvider;
import io.camunda.example.ClientProvider.AuthMethod;
import java.time.Duration;
import java.util.Map;

/**
 * Example application that connects to a cluster on Camunda Cloud, or a locally deployed cluster.
 *
 * <p>It uses {@link ClientProvider} to create a client with the appropriate authentication method.
 */
public class ProcessInstanceWithResultCreator {
  public static void main(final String[] args) {
    final String bpmnProcessId = "demoProcessSingleTask";

    try (final CamundaClient client = ClientProvider.createCamundaClient(AuthMethod.none)) {

      openJobWorker(client); // open job workers so that task are executed and process is completed
      System.out.println("Creating process instance");

      final ProcessInstanceResult processInstanceResult =
          client
              .newCreateInstanceCommand()
              .bpmnProcessId(bpmnProcessId)
              .latestVersion()
              .withResult() // to await the completion of process execution and return result
              .execute();

      System.out.println(
          "Process instance created with key: "
              + processInstanceResult.getProcessInstanceKey()
              + " and completed with results: "
              + processInstanceResult.getVariables());
    }
  }

  private static void openJobWorker(final CamundaClient client) {
    client
        .newWorker()
        .jobType("foo")
        .handler(
            (jobClient, job) ->
                jobClient
                    .newCompleteCommand(job.getKey())
                    .variables(Map.of("job", job.getKey()))
                    .send())
        .timeout(Duration.ofSeconds(10))
        .open();
  }
}
