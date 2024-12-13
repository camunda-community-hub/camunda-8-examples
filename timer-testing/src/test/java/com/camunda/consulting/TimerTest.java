package com.camunda.consulting;

import static io.camunda.process.test.api.CamundaAssert.*;
import static org.assertj.core.api.Assertions.*;

import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@CamundaProcessTest
public class TimerTest {
  private static final Duration DEFAULT = Duration.ofSeconds(30);
  CamundaProcessTestContext camundaProcessTestContext;
  ZeebeClient zeebeClient;

  @BeforeEach
  void deploy() {
    zeebeClient.newDeployResourceCommand().addResourceFromClasspath("get-up.bpmn").send().join();
  }

  @Test
  void happyPath() throws InterruptedException, TimeoutException {
    ProcessInstanceEvent processInstance = createInstance("GetUpProcess");
    assertThat(processInstance).hasActiveElements("Tell kids to get up");
    completeJob("callKids");
    assertThat(processInstance).hasActiveElements("Wait 15 minutes");
    skipTimer(Duration.ofMinutes(15));
    assertThat(processInstance).hasActiveElements("Check current progress");
    completeJob("checkProgress", Map.of("progress", "ready"));
    assertThat(processInstance).hasActiveElements("Wait another 15 minutes");
    skipTimer(Duration.ofMinutes(15));
    assertThat(processInstance).hasActiveElements("Wait another 5 minutes");
    skipTimer(Duration.ofMinutes(5));
    assertThat(processInstance).isCompleted().hasCompletedElements("Kids are fed");
  }

  @Test
  void boundaryEvent() throws InterruptedException, TimeoutException {
    ProcessInstanceEvent processInstance = createInstance("GetUpProcess");
    assertThat(processInstance).hasActiveElements("Tell kids to get up");
    completeJob("callKids");
    assertThat(processInstance).hasActiveElements("Wait 15 minutes");
    skipTimer(Duration.ofMinutes(15));
    assertThat(processInstance).hasActiveElements("Check current progress");
    completeJob("checkProgress", Map.of("progress", "in bed"));
    assertThat(processInstance).hasActiveElements("Tell kids to get up");
    completeJob("callKids");
    assertThat(processInstance).hasActiveElements("Wait 15 minutes");
    skipTimer(Duration.ofMinutes(15));
    assertThat(processInstance).hasActiveElements("Check current progress");
    completeJob("checkProgress", Map.of("progress", "in bed"));
    assertThat(processInstance).hasActiveElements("Tell kids to get up");
    completeJob("callKids");
    assertThat(processInstance).hasActiveElements("Wait 15 minutes");
    skipTimer(Duration.ofMinutes(15));
    assertThat(processInstance).hasActiveElements("Check current progress");
    completeJob("checkProgress", Map.of("progress", "dressing"));
    skipTimer(Duration.ofMinutes(5));
    assertThat(processInstance).hasActiveElements("Check current progress");
    completeJob("checkProgress", Map.of("progress", "ready"));
    assertThat(processInstance).hasActiveElements("Wait another 15 minutes");
    skipTimer(Duration.ofMinutes(15));
    assertThat(processInstance).hasActiveElements("Wait another 5 minutes");
    skipTimer(Duration.ofMinutes(5));
    assertThat(processInstance).isCompleted().hasCompletedElements("Make them ready");
    // missing: hasNotCompletedElements()
  }

  private void skipTimer(Duration timerDuration) {
    camundaProcessTestContext.increaseTime(timerDuration);
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

  private void completeJob(String jobType) throws InterruptedException, TimeoutException {
    completeJob(jobType, Map.of());
  }

  private void completeJob(String jobType, Map<String, Object> variables) {
    ActivatedJob activatedJob = job(jobType);
    complete(activatedJob.getKey(), variables);
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
