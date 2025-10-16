package org.camunda;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.externalTask;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.externalTaskService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;

import java.util.Map;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.community.process_test_coverage.junit5.platform7.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ProcessEngineCoverageExtension.class)
public class TestC7Process {

  public static final String BPMN_DIAGRAM = "bpmn/hybrid-process-c7.bpmn";
  public static final String MAIN_PROCESS = "C7_First";
  public static final String CALLED_PROCESS = "C7_Second";
  ProcessEngine processEngine;

  @Test
  @Deployment(resources = BPMN_DIAGRAM)
  public void startInstance() {
    ProcessInstance instance = processEngine
        .getRuntimeService()
        .createProcessInstanceByKey(MAIN_PROCESS)
        .setVariables(Map.of("payload", "Hello World", "version", 1, "bpmnProcessId", "example"))
        .execute();

    assertThat(instance).isWaitingAt("StartInstanceInC8");
  }

  @Test
  @Deployment(resources = BPMN_DIAGRAM)
  public void receiveAndSendMessage(){
    ProcessInstance instance = processEngine
        .getRuntimeService()
        .createProcessInstanceByKey(MAIN_PROCESS)
        .startAfterActivity("StartInstanceInC8")
        .execute();

    assertThat(instance).isWaitingAt("MessageReceivedFromC8");

    processEngine.getRuntimeService()
        .correlateMessage("message-c8");

    assertThat(instance).isWaitingAt("SendMessageToC8");
  }

  @Test
  @Deployment(resources = BPMN_DIAGRAM)
  public void variableUpdated(){
    ProcessInstance instance = processEngine
        .getRuntimeService()
        .createProcessInstanceByKey(MAIN_PROCESS)
        .startAfterActivity("SendMessageToC8")
        .execute();

    assertThat(instance).isWaitingAt("EventBasedGateway");

    processEngine.getRuntimeService()
        .setVariable(instance.getId(), "variable", true);

    assertThat(instance).isWaitingAt("StartC8Process");
  }

  @Test
  @Deployment(resources = BPMN_DIAGRAM)
  public void signalReceived(){
    ProcessInstance instance = processEngine
        .getRuntimeService()
        .createProcessInstanceByKey(MAIN_PROCESS)
        .startAfterActivity("SendMessageToC8")
        .execute();

    assertThat(instance).isWaitingAt("EventBasedGateway");

    processEngine
        .getRuntimeService()
        .createSignalEvent("MySignal")
        .send();

    assertThat(instance).isWaitingAt("StartC8Process");
  }

  @Test
  @Deployment(resources = BPMN_DIAGRAM)
  public void callC8ProcessWithoutErrors(){
    ProcessInstance instance = processEngine
        .getRuntimeService()
        .createProcessInstanceByKey(MAIN_PROCESS)
        .startAfterActivity("GatewayAfterEventbased")
        .execute();
    assertThat(instance).isWaitingAt("StartC8Process");
    complete(externalTask());
    assertThat(instance).isWaitingAt("completed-message-c7");
    processEngine.getRuntimeService()
        .correlateMessage("message-c7");
    assertThat(instance).isEnded();
  }

  @Test
  @Deployment(resources = BPMN_DIAGRAM)
  public void callC8ProcessWithErrors(){
    ProcessInstance instance = processEngine
        .getRuntimeService()
        .createProcessInstanceByKey(MAIN_PROCESS)
        .startAfterActivity("GatewayAfterEventbased")
        .execute();
    processEngine.getRuntimeService()
        .correlateMessage("error-message-c7");
    assertThat(instance).isWaitingAt("FixManually");
    complete(task());
    assertThat(instance).isEnded();
  }

  @Test
  @Deployment(resources = BPMN_DIAGRAM)
  public void secondC7ProcessHappyPath(){
    ProcessInstance instance = processEngine
        .getRuntimeService()
        .createProcessInstanceByKey(CALLED_PROCESS)
        .setVariable("payload", "Hello World")
        .execute();
    assertThat(instance).isWaitingAt("DoSomething");
    complete(externalTask());
    assertThat(instance).isWaitingAt("ReportSuccessEndEvent");
    complete(externalTask());
    assertThat(instance).isEnded();
  }

  @Test
  @Deployment(resources = BPMN_DIAGRAM)
  public void secondC7ProcessWithError(){
    ProcessInstance instance = processEngine
        .getRuntimeService()
        .createProcessInstanceByKey(CALLED_PROCESS)
        .execute();
    assertThat(instance).isWaitingAt("DoSomething");
    externalTaskService().fetchAndLock(1,"worker-name")
        .topic("do-something", 10000L).execute().forEach(externalTask -> {
      externalTaskService().handleBpmnError(externalTask.getId(),"worker-name","error-code");
            });
    assertThat(instance).isWaitingAt("ReportErrorEndEvent");
    complete(externalTask());
    assertThat(instance).isEnded();
  }


}
