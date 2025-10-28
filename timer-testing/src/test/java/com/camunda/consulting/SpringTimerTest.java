package com.camunda.consulting;

import static com.camunda.consulting.ExampleApplication.*;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.process.test.api.CamundaAssert;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@CamundaSpringProcessTest
@SpringBootTest
public class SpringTimerTest {
  private static final Duration DEFAULT = Duration.ofSeconds(30);
  @Autowired CamundaClient camundaClient;
  @Autowired CamundaProcessTestContext camundaProcessTestContext;

  @BeforeEach
  void deploy() {
    camundaClient.newDeployResourceCommand().addResourceFromClasspath("get-up.bpmn").send().join();
  }

  @AfterEach
  void reset() {
    progressOverride = null;
  }

  @RepeatedTest(10)
  void happyPath() throws InterruptedException, TimeoutException {
    progressOverride = "ready";
    ProcessInstanceEvent processInstance = createInstance("GetUpProcess");
    CamundaAssert.assertThat(processInstance)
        .hasCompletedElement("TellKidsToGetUpTask", 1)
        .hasActiveElement("Wait15MinutesEvent", 1);
    camundaProcessTestContext.increaseTime(Duration.ofMinutes(15));
    CamundaAssert.assertThat(processInstance)
        .hasCompletedElement("Wait15MinutesEvent", 1)
        .hasCompletedElement("CheckCurrentProgressTask", 1)
        .hasActiveElement("Wait15MinutesEvent1", 1);
    camundaProcessTestContext.increaseTime(Duration.ofMinutes(15));
    CamundaAssert.assertThat(processInstance)
        .hasCompletedElement("Wait15MinutesEvent1", 1)
        .hasActiveElement("Wait5MinutesEvent1", 1);
    camundaProcessTestContext.increaseTime(Duration.ofMinutes(5));
    CamundaAssert.assertThat(processInstance).isCompleted();
  }

  @RepeatedTest(10)
  void boundaryEvent() throws InterruptedException, TimeoutException {
    progressOverride = "in bed";
    ProcessInstanceEvent processInstance = createInstance("GetUpProcess");
    CamundaAssert.assertThat(processInstance)
        .hasCompletedElement("TellKidsToGetUpTask", 1)
        .hasActiveElement("Wait15MinutesEvent", 1);
    camundaProcessTestContext.increaseTime(Duration.ofMinutes(15));
    CamundaAssert.assertThat(processInstance)
        .hasCompletedElement("Wait15MinutesEvent", 1)
        .hasCompletedElement("CheckCurrentProgressTask", 1)
        .hasCompletedElement("TellKidsToGetUpTask", 2)
        .hasActiveElement("Wait15MinutesEvent", 1);
    camundaProcessTestContext.increaseTime(Duration.ofMinutes(15));
    CamundaAssert.assertThat(processInstance)
        .hasCompletedElement("Wait15MinutesEvent", 2)
        .hasCompletedElement("CheckCurrentProgressTask", 2)
        .hasCompletedElement("TellKidsToGetUpTask", 3)
        .hasActiveElement("Wait15MinutesEvent", 1);
    progressOverride = "dressing";
    camundaProcessTestContext.increaseTime(Duration.ofMinutes(15));
    CamundaAssert.assertThat(processInstance)
        .hasCompletedElement("CheckCurrentProgressTask", 3)
        .hasActiveElement("Wait5MinutesEvent", 1);
    progressOverride = "ready";
    camundaProcessTestContext.increaseTime(Duration.ofMinutes(5));
    CamundaAssert.assertThat(processInstance)
        .hasCompletedElement("CheckCurrentProgressTask", 4)
        .hasActiveElement("Wait15MinutesEvent1", 1);
    // we start having breakfast (finally)
    camundaProcessTestContext.increaseTime(Duration.ofMinutes(10));
    // we are running late at this point (15+15+15+5+10 = 60) so the boundary event gets triggered
    // instead
    CamundaAssert.assertThat(processInstance)
        .hasCompletedElement("RunningLateBoundaryEvent", 1)
        .hasActiveElement("Wait5MinutesEvent1", 1);
    camundaProcessTestContext.increaseTime(Duration.ofMinutes(5));
    CamundaAssert.assertThat(processInstance)
        .isCompleted()
        .hasCompletedElement("MakeThemReadyTask", 1)
        .hasNotActivatedElements("KidsAreFedEndEvent");
  }

  private ProcessInstanceEvent createInstance(String bpmnProcessId) {
    return camundaClient
        .newCreateInstanceCommand()
        .bpmnProcessId(bpmnProcessId)
        .latestVersion()
        .send()
        .join();
  }
}
