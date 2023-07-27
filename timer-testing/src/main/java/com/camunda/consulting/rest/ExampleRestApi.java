package com.camunda.consulting.rest;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExampleRestApi {

  private static final Logger LOG = LoggerFactory.getLogger(ExampleRestApi.class);

  private final ZeebeClient zeebeClient;

  @Autowired
  public ExampleRestApi(ZeebeClient zeebeClient) {
    this.zeebeClient = zeebeClient;
  }

  @PostMapping("/start-example")
  public ResponseEntity<String> startExampleProcess() {
    ProcessInstanceEvent processInstanceEvent =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("GetUpProcess")
            .latestVersion()
            .send()
            .join();

    return ResponseEntity.status(HttpStatus.OK)
        .body("Started: " + processInstanceEvent.getProcessInstanceKey());
  }
}
