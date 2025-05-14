/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.example.process;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.example.ClientProvider;
import io.camunda.zeebe.example.ClientProvider.AuthMethod;

/**
 * Example application that connects to a cluster on Camunda Cloud, or a locally deployed cluster.
 *
 * <p>It uses {@link ClientProvider} to create a client with the appropriate authentication method.
 */
public final class ProcessInstanceCreator {

  public static void main(final String[] args) {
    final String bpmnProcessId = "demoProcess";

    try (final ZeebeClient client = ClientProvider.createZeebeClient(AuthMethod.none)) {

      System.out.println("Creating process instance");

      final ProcessInstanceEvent processInstanceEvent =
          client
              .newCreateInstanceCommand()
              .bpmnProcessId(bpmnProcessId)
              .latestVersion()
              .send()
              .join();

      System.out.println(
          "Process instance created with key: " + processInstanceEvent.getProcessInstanceKey());
    }
  }
}
