package io.camunda.example.e2e.process;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.response.DeploymentEvent;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.client.api.worker.JobClient;
import io.camunda.client.api.worker.JobHandler;
import io.camunda.client.api.worker.JobWorker;
import io.camunda.example.ClientProvider;
import java.time.Duration;
import java.util.Map;

/**
 * Example application that connects to a cluster on Camunda Cloud, or a locally deployed cluster.
 *
 * <p>It uses {@link ClientProvider} to create a client with the appropriate authentication method.
 */
public class DeployAndComplete {

  public static void main(final String[] args) {
    final String bpmnProcessId = "exampleProcess";

    try (final CamundaClient client =
        ClientProvider.createCamundaClient(ClientProvider.AuthMethod.none)) {

      System.out.println("Deploying process definition...");
      final DeploymentEvent deploymentEvent =
          client
              .newDeployResourceCommand()
              .addResourceFromClasspath("exampleProcess.bpmn")
              .send()
              .join();

      System.out.println("Deployment successful: " + deploymentEvent.getKey());

      // Create a process instance
      System.out.println("Creating process instance...");
      final ProcessInstanceEvent processInstanceEvent =
          client
              .newCreateInstanceCommand()
              .bpmnProcessId(bpmnProcessId)
              .latestVersion()
              .variables(Map.of("orderId", "12345", "amount", 100.0))
              .send()
              .join();

      System.out.println(
          "Process instance created: " + processInstanceEvent.getProcessInstanceKey());

      // Register a job worker to handle jobs of type "send-email"
      System.out.println("Registering job worker...");

      // The job type must match the one defined in the BPMN process
      final String jobType = "send-email";

      try (final JobWorker ignored =
          client.newWorker().jobType(jobType).handler(new EmailJobHandler()).open()) {

        System.out.println("Job worker opened and receiving jobs of type: " + jobType);

        // Keep the worker running
        Thread.sleep(Duration.ofMinutes(10).toMillis());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }
    }
  }

  private static class EmailJobHandler implements JobHandler {
    @Override
    public void handle(final JobClient client, final ActivatedJob job) {
      // Extract variables from the job
      final Map<String, Object> variables = job.getVariablesAsMap();

      // Perform your business logic here
      System.out.println(
          "Processing job: "
              + job.getKey()
              + " for process instance: "
              + job.getProcessInstanceKey());
      System.out.println("Job variables: " + variables);

      // Complete the job (or use client.newFailCommand() if something goes wrong)
      client.newCompleteCommand(job.getKey()).variables(Map.of("emailSent", true)).send().join();

      System.out.println("Job completed successfully: " + job.getKey());
    }
  }
}
