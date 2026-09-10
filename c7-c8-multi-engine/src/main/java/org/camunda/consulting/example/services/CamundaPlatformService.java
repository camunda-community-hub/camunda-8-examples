package org.camunda.consulting.example.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.camunda.community.rest.client.api.MessageApi;
import org.camunda.community.rest.client.api.ProcessDefinitionApi;
import org.camunda.community.rest.client.api.SignalApi;
import org.camunda.community.rest.client.dto.CorrelationMessageDto;
import org.camunda.community.rest.client.dto.SignalDto;
import org.camunda.community.rest.client.dto.StartProcessInstanceDto;
import org.camunda.community.rest.client.dto.VariableValueDto;
import org.camunda.community.rest.client.invoker.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CamundaPlatformService implements EngineService{

  private static final Logger LOG = LoggerFactory.getLogger(CamundaPlatformService.class);

  private MessageApi messageApi;
  private SignalApi signalApi;
  private ProcessDefinitionApi processDefinitionApi;

  public CamundaPlatformService(MessageApi messageApi, SignalApi signalApi, ProcessDefinitionApi processDefinitionApi) {
    this.messageApi = messageApi;
    this.signalApi = signalApi;
    this.processDefinitionApi = processDefinitionApi;
  }


  @Override
  public Object startInstance(String bpmnProcessId, String correlationKey, Optional<Object> payload,
      Optional<Integer> version) {

    Map<String,VariableValueDto> variables = new HashMap<>();
    variables.put("correlationKey", new VariableValueDto().value(correlationKey));

    if(payload.isPresent()) {
      variables.put("payload", new VariableValueDto().value(payload.get()));
    }
    try {
      StartProcessInstanceDto startInstance = new StartProcessInstanceDto();
      startInstance.setVariables(variables);

      return processDefinitionApi.startProcessInstanceByKey(bpmnProcessId, startInstance);
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Object sendMessage(String messageName, String correlationKey, Optional<Object> payload) {
    try {
      CorrelationMessageDto message = new CorrelationMessageDto()
          .messageName(messageName)
          .correlationKeys(Map.of("correlationKey",new VariableValueDto().value(correlationKey)));

      if(payload.isPresent()) {
        message.processVariables(Map.of("payload",new VariableValueDto().value(payload.get())));
      }

      return messageApi.deliverMessage(message);
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  public void sendSignal(String signalName, Optional<Object> payload) {
    try {
      SignalDto signal = new SignalDto()
          .name(signalName);

      if(payload.isPresent()) {
          signal.variables(Map.of("payload",new VariableValueDto().value(payload.get())));
      }

      signalApi.throwSignal(signal);
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }
}
