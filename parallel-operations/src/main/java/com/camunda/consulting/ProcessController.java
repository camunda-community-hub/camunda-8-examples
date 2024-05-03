package com.camunda.consulting;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/process")
public class ProcessController {
  @Autowired ZeebeClient zeebeClient;

  @PostMapping
  public ProcessInstanceStartedResponse startProcessInstance() {
    ProcessInstanceEvent process =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("MigratedProcessProcess")
            .latestVersion()
            .send()
            .join();
    return new ProcessInstanceStartedResponse(process.getProcessInstanceKey());
  }

  public record ProcessInstanceStartedResponse(Long processInstanceKey) {}
}
