package com.camunda.consulting;

import static com.camunda.consulting.ExampleApplication.*;
import static io.camunda.zeebe.process.test.assertions.BpmnAssert.*;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.*;
import static org.assertj.core.api.Assertions.*;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.inspections.model.InspectedProcessInstance;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.camunda.community.process_test_coverage.junit5.platform8.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@ZeebeSpringTest
@SpringBootTest
@ExtendWith(ProcessEngineCoverageExtension.class)
public class SpringTimerTest {
  private static final Duration DEFAULT = Duration.ofSeconds(30);
  @Autowired ZeebeTestEngine zeebeTestEngine;
  @Autowired ZeebeClient zeebeClient;

  @BeforeEach
  void deploy() {
    zeebeClient.newDeployResourceCommand().addResourceFromClasspath("get-up.bpmn").send().join();
  }

  @AfterEach
  void reset() {
    progressOverride = null;
  }

  @RepeatedTest(10)
  void happyPath() throws InterruptedException, TimeoutException {
    progressOverride = "ready";
    ProcessInstanceEvent processInstance = createInstance("GetUpProcess");
    InspectedProcessInstance inspectedProcessInstance =
        new InspectedProcessInstance(processInstance.getProcessInstanceKey());
    waitForProcessInstanceHasPassedElement(processInstance, "TellKidsToGetUpTask", DEFAULT);
    skipTimer(inspectedProcessInstance, "Wait15MinutesEvent", Duration.ofMinutes(15));
    waitForProcessInstanceHasPassedElement(processInstance, "CheckCurrentProgressTask", DEFAULT);
    skipTimer(inspectedProcessInstance, "Wait15MinutesEvent1", Duration.ofMinutes(15));
    skipTimer(inspectedProcessInstance, "Wait5MinutesEvent1", Duration.ofMinutes(5));
    waitForProcessInstanceCompleted(processInstance);
  }

  @RepeatedTest(10)
  void boundaryEvent() throws InterruptedException, TimeoutException {
    progressOverride = "in bed";
    ProcessInstanceEvent processInstance = createInstance("GetUpProcess");
    InspectedProcessInstance inspectedProcessInstance =
        new InspectedProcessInstance(processInstance.getProcessInstanceKey());
    waitForProcessInstanceHasPassedElement(processInstance, "TellKidsToGetUpTask", DEFAULT);
    skipTimer(inspectedProcessInstance, "Wait15MinutesEvent", Duration.ofMinutes(15));
    waitForProcessInstanceHasPassedElement(processInstance, "CheckCurrentProgressTask", DEFAULT);
    waitForProcessInstanceHasPassedElement(
        inspectedProcessInstance, "TellKidsToGetUpTask", DEFAULT, 2);
    skipTimer(inspectedProcessInstance, "Wait15MinutesEvent", Duration.ofMinutes(15), 2);
    waitForProcessInstanceHasPassedElement(
        inspectedProcessInstance, "CheckCurrentProgressTask", DEFAULT, 2);
    progressOverride = "dressing";
    waitForProcessInstanceHasPassedElement(
        inspectedProcessInstance, "TellKidsToGetUpTask", DEFAULT, 3);
    skipTimer(inspectedProcessInstance, "Wait15MinutesEvent", Duration.ofMinutes(15), 3);
    waitForProcessInstanceHasPassedElement(
        inspectedProcessInstance, "CheckCurrentProgressTask", DEFAULT, 3);
    progressOverride = "ready";
    skipTimer(inspectedProcessInstance, "Wait5MinutesEvent", Duration.ofMinutes(5));
    waitForProcessInstanceHasPassedElement(
        inspectedProcessInstance, "CheckCurrentProgressTask", DEFAULT, 4);
    // we start having breakfast (finally)
    zeebeTestEngine.increaseTime(Duration.ofMinutes(10));
    // we are running late at this point (15+15+15+5+10 = 60) so the boundary event gets triggered
    // instead
    waitForProcessInstanceHasPassedElement(processInstance, "RunningLateBoundaryEvent", DEFAULT);
    skipTimer(inspectedProcessInstance, "Wait5MinutesEvent1", Duration.ofMinutes(5));
    waitForProcessInstanceCompleted(processInstance);
    assertThat(processInstance)
        .isCompleted()
        .hasPassedElement("MakeThemReadyTask")
        .hasNotPassedElement("KidsAreFedEndEvent");
  }

  private void skipTimer(
      InspectedProcessInstance processInstance, String timerElementId, Duration timerDuration)
      throws InterruptedException, TimeoutException {
    skipTimer(processInstance, timerElementId, timerDuration, 1);
  }

  private void skipTimer(
      InspectedProcessInstance processInstance,
      String timerElementId,
      Duration timerDuration,
      int times)
      throws InterruptedException, TimeoutException {
    zeebeTestEngine.waitForIdleState(DEFAULT);
    assertThat(processInstance).isWaitingAtElements(timerElementId);
    zeebeTestEngine.increaseTime(timerDuration);
    waitForProcessInstanceHasPassedElement(processInstance, timerElementId, DEFAULT, times);
  }

  private ProcessInstanceEvent createInstance(String bpmnProcessId)
      throws InterruptedException, TimeoutException {
    ProcessInstanceEvent instance =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId(bpmnProcessId)
            .latestVersion()
            .send()
            .join();
    return instance;
  }
}
