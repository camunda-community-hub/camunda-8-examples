package com.camunda.consulting;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.*;
import static org.assertj.core.api.Assertions.*;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.camunda.community.process_test_coverage.junit5.platform8.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ZeebeProcessTest
@ExtendWith(ProcessEngineCoverageExtension.class)
@Disabled
public class TimerTest {
  private static final Duration DEFAULT = Duration.ofSeconds(30);
  ZeebeTestEngine zeebeTestEngine;
  ZeebeClient zeebeClient;

  @BeforeEach
  void deploy() {
    zeebeClient.newDeployResourceCommand().addResourceFromClasspath("get-up.bpmn").send().join();
  }

  @Test
  void happyPath() throws InterruptedException, TimeoutException {
    ProcessInstanceEvent processInstance = createInstance("GetUpProcess");
    assertThat(processInstance).isWaitingAtElements("TellKidsToGetUpTask");
    completeJob("callKids");
    assertThat(processInstance).isWaitingAtElements("Wait15MinutesEvent");
    skipTimer(Duration.ofMinutes(15));
    assertThat(processInstance).isWaitingAtElements("CheckCurrentProgressTask");
    completeJob("checkProgress", Map.of("progress", "ready"));
    assertThat(processInstance).isWaitingAtElements("Wait15MinutesEvent1");
    skipTimer(Duration.ofMinutes(15));
    assertThat(processInstance).isWaitingAtElements("Wait5MinutesEvent1");
    skipTimer(Duration.ofMinutes(5));
    assertThat(processInstance).isCompleted().hasPassedElement("KidsAreFedEndEvent");
  }

  @Test
  void boundaryEvent() throws InterruptedException, TimeoutException {
    ProcessInstanceEvent processInstance = createInstance("GetUpProcess");
    assertThat(processInstance).isWaitingAtElements("TellKidsToGetUpTask");
    completeJob("callKids");
    assertThat(processInstance).isWaitingAtElements("Wait15MinutesEvent");
    skipTimer(Duration.ofMinutes(15));
    assertThat(processInstance).isWaitingAtElements("CheckCurrentProgressTask");
    completeJob("checkProgress", Map.of("progress", "in bed"));
    assertThat(processInstance).isWaitingAtElements("TellKidsToGetUpTask");
    completeJob("callKids");
    assertThat(processInstance).isWaitingAtElements("Wait15MinutesEvent");
    skipTimer(Duration.ofMinutes(15));
    assertThat(processInstance).isWaitingAtElements("CheckCurrentProgressTask");
    completeJob("checkProgress", Map.of("progress", "in bed"));
    assertThat(processInstance).isWaitingAtElements("TellKidsToGetUpTask");
    completeJob("callKids");
    assertThat(processInstance).isWaitingAtElements("Wait15MinutesEvent");
    skipTimer(Duration.ofMinutes(15));
    assertThat(processInstance).isWaitingAtElements("CheckCurrentProgressTask");
    completeJob("checkProgress", Map.of("progress", "dressing"));
    skipTimer(Duration.ofMinutes(5));
    assertThat(processInstance).isWaitingAtElements("CheckCurrentProgressTask");
    completeJob("checkProgress", Map.of("progress", "ready"));
    assertThat(processInstance).isWaitingAtElements("Wait15MinutesEvent1");
    skipTimer(Duration.ofMinutes(15));
    assertThat(processInstance).isWaitingAtElements("Wait5MinutesEvent1");
    skipTimer(Duration.ofMinutes(5));
    assertThat(processInstance)
        .isCompleted()
        .hasPassedElement("MakeThemReadyTask")
        .hasNotPassedElement("KidsAreFedEndEvent");
  }

  private void skipTimer(Duration timerDuration) throws InterruptedException, TimeoutException {
    zeebeTestEngine.waitForIdleState(DEFAULT);
    zeebeTestEngine.increaseTime(timerDuration);
    zeebeTestEngine.waitForIdleState(DEFAULT);
    zeebeTestEngine.waitForBusyState(DEFAULT);
    zeebeTestEngine.waitForIdleState(DEFAULT);
  }

  private ProcessInstanceEvent createInstance(String bpmnProcessId)
      throws InterruptedException, TimeoutException {
    ProcessInstanceEvent instance =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("GetUpProcess")
            .latestVersion()
            .send()
            .join();
    zeebeTestEngine.waitForIdleState(DEFAULT);
    return instance;
  }

  private void completeJob(String jobType) throws InterruptedException, TimeoutException {
    completeJob(jobType, Map.of());
  }

  private void completeJob(String jobType, Map<String, Object> variables)
      throws InterruptedException, TimeoutException {
    ActivatedJob activatedJob = job(jobType);
    complete(activatedJob.getKey(), variables);
    zeebeTestEngine.waitForIdleState(DEFAULT);
  }

  private void complete(long jobKey, Map<String, Object> variables) {
    zeebeClient.newCompleteCommand(jobKey).variables(variables).send().join();
  }

  private ActivatedJob job(String jobType) {
    List<ActivatedJob> jobs =
        zeebeClient
            .newActivateJobsCommand()
            .jobType(jobType)
            .maxJobsToActivate(1)
            .timeout(Duration.ofMillis(1))
            .send()
            .join()
            .getJobs();
    assertThat(jobs).hasSize(1);
    return jobs.get(0);
  }
}
