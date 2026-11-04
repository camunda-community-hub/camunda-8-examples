package com.camunda.consulting;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcessController {
  private final CamundaClient zeebeClient;

  public ProcessController(CamundaClient zeebeClient) {
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
