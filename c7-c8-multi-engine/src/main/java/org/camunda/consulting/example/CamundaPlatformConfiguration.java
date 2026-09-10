package org.camunda.consulting.example;

import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import java.util.Optional;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.spring.boot.starter.ClientProperties;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.consulting.example.services.ZeebeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaPlatformConfiguration {

  private final ZeebeService zeebeService;

  protected static Logger LOG = LoggerFactory.getLogger(CamundaPlatformConfiguration.class);

  protected String workerId;

  public CamundaPlatformConfiguration(ClientProperties properties, ZeebeService zeebeService) {
    workerId = properties.getWorkerId();
    this.zeebeService = zeebeService;
  }

  @ExternalTaskSubscription("start-c8-instance")
  @Bean
  public ExternalTaskHandler startC8Instance() {
    return (externalTask, externalTaskService) -> {
      try {
        String bpmnProcessId = externalTask.getVariable("bpmnProcessId");
        String correlationKey = externalTask.getVariable("correlationKey");
        Optional<Object> payload = Optional.ofNullable(externalTask.getVariable("payload"));
        Optional<Integer> version = Optional.ofNullable(externalTask.getVariable("version"));
        ProcessInstanceEvent processInstanceEvent = (ProcessInstanceEvent) zeebeService.startInstance(bpmnProcessId, correlationKey, payload, version);
        externalTaskService.complete(externalTask, Variables.putValue("processInstanceEvent", processInstanceEvent));
        LOG.info("Started instance for processId: " + bpmnProcessId + " with key: " + processInstanceEvent.getProcessInstanceKey());
      } catch (Exception e) {
        LOG.error("Error starting instance", e);
        externalTaskService.handleFailure(externalTask, workerId, e.getMessage(), 0, 0);
      }
    };
  }

  @ExternalTaskSubscription("message-to-c8")
  @Bean
  public ExternalTaskHandler messageToC8() {
    return (externalTask, externalTaskService) -> {
      String messageName = externalTask.getVariable("messageName");
      String correlationKey = externalTask.getVariable("correlationKey");
      Optional<Object> payload = Optional.ofNullable(externalTask.getVariable("payload"));
      try {
        PublishMessageResponse messageResponse = (PublishMessageResponse) zeebeService.sendMessage(messageName, correlationKey, payload);
        LOG.info("Sent message: " + messageName + " with correlationKey: " + correlationKey);
        externalTaskService.complete(externalTask, Variables.putValue("messageResponse", messageResponse));
      } catch (Exception e) {
        LOG.error("Error sending message", e);
        externalTaskService.handleFailure(externalTask, workerId, e.getMessage(), 0, 0);
      }
    };
  }

  @ExternalTaskSubscription("do-something")
  @Bean
  public ExternalTaskHandler doSomething() {
    return (externalTask, externalTaskService) -> {
      try {
        LOG.info("Doing something");
        externalTaskService.complete(externalTask);
      } catch (Exception e) {
        LOG.error("Error doing something", e);
        externalTaskService.handleFailure(externalTask, workerId, e.getMessage(), 0, 0);
      }
    };
  }

}
