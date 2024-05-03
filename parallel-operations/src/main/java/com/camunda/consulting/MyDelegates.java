package com.camunda.consulting;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import java.util.Map;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MyDelegates {
  private static final Logger LOG = LoggerFactory.getLogger(MyDelegates.class);
  private final RuntimeService runtimeService;
  private final ZeebeClient zeebeClient;
  private final StringService stringService;

  public MyDelegates(
      RuntimeService runtimeService, ZeebeClient zeebeClient, StringService stringService) {
    this.runtimeService = runtimeService;
    this.zeebeClient = zeebeClient;
    this.stringService = stringService;
  }

  public OldTaskOneResponse oldTaskOne() {
    return new OldTaskOneResponse(stringService.get());
  }

  public OldTaskTwoResponse oldTaskTwo(String someOtherText) {
    return new OldTaskTwoResponse(someOtherText + stringService.get());
  }

  @JobWorker
  public FirstTaskResponse firstTask() {
    return new FirstTaskResponse(stringService.get());
  }

  @JobWorker
  public SharedTaskResponse sharedTask(@Variable String someText) {
    return new SharedTaskResponse(someText + stringService.get());
  }

  @JobWorker
  public AnotherTaskResponse anotherTask(@Variable String someOtherText) {
    return new AnotherTaskResponse(someOtherText + stringService.get());
  }

  @JobWorker
  public StartOldProcessResponse startOldProcess() {
    String callbackId = stringService.get();
    ProcessInstance oldProcess =
        runtimeService.startProcessInstanceByKey(
            "OldProcessProcess", stringService.get(), Map.of("callbackId", callbackId));
    return new StartOldProcessResponse(callbackId);
  }

  public void continueMigratedProcess(String callbackId) {
    zeebeClient
        .newPublishMessageCommand()
        .messageName("oldProcessComplete")
        .correlationKey(callbackId)
        .send()
        .join();
  }

  public record OldTaskOneResponse(String someText) {}

  public record FirstTaskResponse(String someText) {}

  public record SharedTaskResponse(String someOtherText) {}

  public record OldTaskTwoResponse(String lastText) {}

  public record AnotherTaskResponse(String lastText) {}

  public record StartOldProcessResponse(String callbackId) {}
}
