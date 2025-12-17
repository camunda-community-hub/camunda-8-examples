package org.camunda.consulting.example.services;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.CreateProcessInstanceCommandStep1.CreateProcessInstanceCommandStep2;
import io.camunda.zeebe.client.api.command.CreateProcessInstanceCommandStep1.CreateProcessInstanceCommandStep3;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ZeebeService implements EngineService {

  private final ZeebeClient zeebeClient;

  public ZeebeService(ZeebeClient zeebeClient) {
    this.zeebeClient = zeebeClient;
  }

  @Override
  public Object startInstance(String bpmnProcessId, String correlationKey, Optional<Object> payload, Optional<Integer> version) {

    Map<String,Object> variables = new HashMap<>();
    variables.put("correlationKey", correlationKey);

    if(payload.isPresent()) {
      variables.put("payload", payload.get());
    }

    CreateProcessInstanceCommandStep2 step1 = zeebeClient.newCreateInstanceCommand().bpmnProcessId(bpmnProcessId);
    CreateProcessInstanceCommandStep3 step2;
    if(version.isPresent()) {
      step2 = step1.version(version.get());
    } else {
      step2 = step1.latestVersion();
    }
    return step2
        .variables(variables)
        .send()
        .join();
  }

  @Override
  public Object sendMessage(String messageName,String correlationKey, Optional<Object> payload) {
    return zeebeClient.newPublishMessageCommand()
        .messageName(messageName)
        .correlationKey(correlationKey)
        .variables(Map.of("payload",payload))
        .send()
        .join();
  }

}
