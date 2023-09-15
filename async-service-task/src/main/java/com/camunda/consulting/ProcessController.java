package com.camunda.consulting;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcessController {
  private final ZeebeClient zeebeClient;

  public ProcessController(ZeebeClient zeebeClient) {
    this.zeebeClient = zeebeClient;
  }

  @PostMapping("/start")
  public ProcessInstanceEvent start() {
    return zeebeClient
        .newCreateInstanceCommand()
        .bpmnProcessId("AsyncServiceTaskProcess")
        .latestVersion()
        .send()
        .join();
  }
}
