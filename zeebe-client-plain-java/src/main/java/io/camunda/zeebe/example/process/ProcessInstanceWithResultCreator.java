/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.example.process;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import io.camunda.zeebe.example.ClientProvider;
import io.camunda.zeebe.example.ClientProvider.AuthMethod;
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

    try (final ZeebeClient client = ClientProvider.createZeebeClient(AuthMethod.none)) {

      openJobWorker(client); // open job workers so that task are executed and process is completed
      System.out.println("Creating process instance");

      final ProcessInstanceResult processInstanceResult =
          client
              .newCreateInstanceCommand()
              .bpmnProcessId(bpmnProcessId)
              .latestVersion()
              .withResult() // to await the completion of process execution and return result
              .send()
              .join();

      System.out.println(
          "Process instance created with key: "
              + processInstanceResult.getProcessInstanceKey()
              + " and completed with results: "
              + processInstanceResult.getVariables());
    }
  }

  private static void openJobWorker(final ZeebeClient client) {
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
