/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.example.job;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import io.camunda.client.api.worker.JobHandler;
import io.camunda.client.api.worker.JobWorker;
import io.camunda.example.ClientProvider;
import io.camunda.example.ClientProvider.AuthMethod;
import java.time.Duration;
import java.util.Scanner;

/**
 * Example application that connects to a cluster on Camunda Cloud, or a locally deployed cluster.
 *
 * <p>It uses {@link ClientProvider} to create a client with the appropriate authentication method.
 */
public final class JobWorkerCreator {
  public static void main(final String[] args) {
    final String jobType = "foo";

    try (final CamundaClient client = ClientProvider.createCamundaClient(AuthMethod.none)) {

      System.out.println("Opening job worker.");

      try (final JobWorker workerRegistration =
          client
              .newWorker()
              .jobType(jobType)
              .handler(new ExampleJobHandler())
              .timeout(Duration.ofSeconds(10))
              .open()) {
        System.out.println("Job worker opened and receiving jobs.");

        // run until System.in receives exit command
        waitUntilSystemInput("exit");
      }
    }
  }

  private static void waitUntilSystemInput(final String exitCode) {
    try (final Scanner scanner = new Scanner(System.in)) {
      while (scanner.hasNextLine()) {
        final String nextLine = scanner.nextLine();
        if (nextLine.contains(exitCode)) {
          return;
        }
      }
    }
  }

  private static class ExampleJobHandler implements JobHandler {
    @Override
    public void handle(final JobClient client, final ActivatedJob job) {
      // here: business logic that is executed with every job
      System.out.println(job);
      client.newCompleteCommand(job.getKey()).send().join();
    }
  }
}
