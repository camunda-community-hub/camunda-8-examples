package com.camunda.consulting;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcessController {
  private final ZeebeClient zeebeClient;

  @Autowired
  public ProcessController(ZeebeClient zeebeClient) {
    this.zeebeClient = zeebeClient;
  }

  @PostMapping("/start")
  public ResponseEntity<ProcessInstanceEvent> start() {
    return ResponseEntity.ok(
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("RollbackOnErrorProcess")
            .latestVersion()
            .send()
            .join());
  }
}
