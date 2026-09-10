package org.camunda.consulting.example;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import java.util.Optional;
import org.camunda.consulting.example.services.CamundaPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ZeebeWorker {

  protected static Logger LOG = LoggerFactory.getLogger(ZeebeWorker.class);

  private CamundaPlatformService camundaPlatformService;

  public ZeebeWorker(CamundaPlatformService camundaPlatformService) {
    this.camundaPlatformService = camundaPlatformService;
  }

  @JobWorker(type="start-c7-instance")
  public void startC7Instance(final ActivatedJob job) {
    String bpmnProcessId = job.getVariablesAsMap().get("bpmnProcessId").toString();
    String correlationKey = job.getVariablesAsMap().get("correlationKey").toString();
    Optional<Object> payload = Optional.ofNullable(job.getVariablesAsMap().get("payload").toString());
    Optional<Integer> version = Optional.ofNullable(job.getVariablesAsMap().get("version") == null ? null : Integer.parseInt(job.getVariablesAsMap().get("version").toString()));
    camundaPlatformService.startInstance(bpmnProcessId, correlationKey, payload, version);
    LOG.info("Started instance for processId: " + bpmnProcessId);
  }

  @JobWorker(type="message-to-c7")
  public void messageToC7(final ActivatedJob job) {
    String messageName = job.getVariablesAsMap().get("messageName").toString();
    String correlationKey = job.getVariablesAsMap().get("correlationKey").toString();
    Optional<Object> payload = Optional.ofNullable(job.getVariablesAsMap().get("payload").toString());
    camundaPlatformService.sendMessage(messageName, correlationKey, payload);
    LOG.info("Sent message: " + messageName + " with correlationKey: " + correlationKey);
  }

  @JobWorker(type="signal-to-c7")
  public void signalToC7(final ActivatedJob job) {
    String signalName = job.getVariablesAsMap().get("signalName").toString();
    Optional<Object> payload = Optional.ofNullable(job.getVariablesAsMap().get("payload").toString());
    camundaPlatformService.sendSignal(signalName, payload);
    LOG.info("Sent signal: " + signalName + " with payload: " + payload);
  }

  @JobWorker(type="do-something")
  public void doSomething(final ActivatedJob job) {
    LOG.info("Doing something");
  }



}
