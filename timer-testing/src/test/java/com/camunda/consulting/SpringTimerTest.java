package com.camunda.consulting;

import static com.camunda.consulting.ExampleApplication.*;
import static io.camunda.process.test.api.CamundaAssert.*;
import static org.assertj.core.api.Assertions.*;

import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@CamundaSpringProcessTest
@SpringBootTest
public class SpringTimerTest {
  private static final Duration DEFAULT = Duration.ofSeconds(30);
  @Autowired CamundaProcessTestContext camundaProcessTestContext;
  @Autowired ZeebeClient zeebeClient;

  @BeforeEach
  void deploy() {
    zeebeClient.newDeployResourceCommand().addResourceFromClasspath("get-up.bpmn").send().join();
  }

  @AfterEach
  void reset() {
    progressOverride = null;
  }

  @Test
  void happyPath() throws InterruptedException, TimeoutException {
    progressOverride = "ready";
    ProcessInstanceEvent processInstance = createInstance("GetUpProcess");
    assertThat(processInstance).hasActiveElements("Wait 15 minutes");
    skipTimer(Duration.ofMinutes(15));
    assertThat(processInstance).hasActiveElements("Wait another 15 minutes");
    skipTimer(Duration.ofMinutes(15));
    assertThat(processInstance).hasActiveElements("Wait another 5 minutes");
    skipTimer(Duration.ofMinutes(5));
    assertThat(processInstance).isCompleted();
  }

  @Test
  void boundaryEvent() throws InterruptedException, TimeoutException {
    progressOverride = "in bed";
    ProcessInstanceEvent processInstance = createInstance("GetUpProcess");
    assertThat(processInstance).hasActiveElements("Wait 15 minutes");
    skipTimer(Duration.ofMinutes(15));
    assertThat(processInstance).hasActiveElements("Wait 15 minutes");
    skipTimer(Duration.ofMinutes(15));
    assertThat(processInstance).hasActiveElements("Wait 15 minutes");
    progressOverride = "dressing";
    skipTimer(Duration.ofMinutes(15));
    assertThat(processInstance).hasActiveElements("Wait 5 minutes");
    progressOverride = "ready";
    skipTimer(Duration.ofMinutes(5));
    // we start having breakfast (finally)
    skipTimer(Duration.ofMinutes(10));
    // we are running late at this point (15+15+15+5+10 = 60) so the boundary event gets triggered
    // instead
    assertThat(processInstance).hasCompletedElements("Running late");
    assertThat(processInstance).hasActiveElements("Wait another 5 minutes");
    skipTimer(Duration.ofMinutes(5));
    assertThat(processInstance).isCompleted().hasCompletedElements("Make them ready");
    // missing: has not completed elements
  }

  private void skipTimer(Duration timerDuration) throws InterruptedException, TimeoutException {
    camundaProcessTestContext.increaseTime(timerDuration);
  }

  private ProcessInstanceEvent createInstance(String bpmnProcessId) {
    ProcessInstanceEvent instance =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId(bpmnProcessId)
            .latestVersion()
            .send()
            .join();
    return instance;
  }

  private void completeJob(String jobType) {
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
