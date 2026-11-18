package org.camunda;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivateJobsResponse;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;
import org.camunda.community.process_test_coverage.junit5.platform8.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ProcessEngineCoverageExtension.class)
@ZeebeProcessTest
public class TestC8Process {

  public static final String MAIN_PROCESS = "C8_First";
  public static final String CALLED_PROCESS = "C8_Second";
  public static final String PAYLOAD = "{\"payload\":\"Hello World\",\"correlationKey\":\"myCorrelationKey\"}";
  public static final String USER_TASK_JOB = "io.camunda.zeebe:userTask";
  private ZeebeTestEngine engine;
  private ZeebeClient client;

  @BeforeEach
  public void before() {
    DeploymentEvent deployment = client.newDeployResourceCommand()
        .addResourceFromClasspath("bpmn/hybrid-process-c8.bpmn")
        .send()
        .join();
    assertThat(deployment).containsProcessesByBpmnProcessId(MAIN_PROCESS);
    assertThat(deployment).containsProcessesByBpmnProcessId(CALLED_PROCESS);
  }

  @Test
  public void sendAndWaitForMessage() {
    ProcessInstanceEvent instanceEvent = client.newCreateInstanceCommand()
        .bpmnProcessId(MAIN_PROCESS)
        .latestVersion()
        .variables(PAYLOAD)
        .send()
        .join();

    assertThat(instanceEvent).isWaitingExactlyAtElements("MessageToC7");

    getAndCompleteJob("message-to-c7");

    assertThat(instanceEvent).isWaitingExactlyAtElements("MessageReceived");

    client.newPublishMessageCommand()
        .messageName("message-c7")
        .correlationKey("myCorrelationKey")
        .send()
        .join();

    assertThat(instanceEvent).isWaitingAtElements("StartInstanceC7");

  }

  @Test
  public void startProcessInC7AndReceiveResult() {
    ProcessInstanceEvent instanceEvent = client.newCreateInstanceCommand()
        .bpmnProcessId(MAIN_PROCESS)
        .latestVersion()
        .startBeforeElement("StartInstanceC7")
        .variables(PAYLOAD)
        .send()
        .join();

    assertThat(instanceEvent).isWaitingAtElements("StartInstanceC7");

    getAndCompleteJob("start-c7-instance");

    assertThat(instanceEvent).isWaitingAtElements("ProcessCompletedMessage");

    client.newPublishMessageCommand()
        .messageName("message-c7")
        .correlationKey("myCorrelationKey")
        .send()
        .join();

    assertThat(instanceEvent).isWaitingExactlyAtElements("SendSignal");
    getAndCompleteJob("signal-to-c7");
    assertThat(instanceEvent).isCompleted();
  }

  @Test
  public void startProcessInC7AndFails() {
    ProcessInstanceEvent instanceEvent = client.newCreateInstanceCommand()
        .bpmnProcessId(MAIN_PROCESS)
        .latestVersion()
        .startBeforeElement("StartInstanceC7")
        .variables(PAYLOAD)
        .send()
        .join();

    assertThat(instanceEvent).isWaitingAtElements("StartInstanceC7");

    getAndCompleteJob("start-c7-instance");

    assertThat(instanceEvent).isWaitingAtElements("ProcessCompletedMessage");

    client.newPublishMessageCommand()
        .messageName("error-message-c7")
        .correlationKey("myCorrelationKey")
        .variables(PAYLOAD)
        .send()
        .join();

    assertThat(instanceEvent).isWaitingExactlyAtElements("FixManuallyC8");
    getAndCompleteJob(USER_TASK_JOB);
  }

  @Test
  public void startSecondProcessHappyPath() {
    ProcessInstanceEvent instanceEvent = client.newCreateInstanceCommand()
        .bpmnProcessId(CALLED_PROCESS)
        .latestVersion()
        .variables(PAYLOAD)
        .send()
        .join();
    getAndCompleteJob("do-something");
    getAndCompleteJob("message-to-c7");
    assertThat(instanceEvent).isCompleted();
  }

  @Test
  public void startSecondProcessAndFails() {
    ProcessInstanceEvent instanceEvent = client.newCreateInstanceCommand()
        .bpmnProcessId(CALLED_PROCESS)
        .latestVersion()
        .variables(PAYLOAD)
        .send()
        .join();

    getJobAndThrow("do-something");
    assertThat(instanceEvent).isWaitingExactlyAtElements("ReportError");
    getAndCompleteJob("message-to-c7");
    assertThat(instanceEvent).isCompleted();
  }

  private void getAndCompleteJob(String jobType) {
    ActivateJobsResponse jobResponse = client.newActivateJobsCommand()
        .jobType(jobType)
        .maxJobsToActivate(1)
        .send()
        .join();

    assertEquals(jobResponse.getJobs().size(), 1);

    client.newCompleteCommand(jobResponse.getJobs().get(0).getKey())
        .send()
        .join();
  }

  private void getJobAndThrow(String jobType) {
    ActivateJobsResponse jobResponse = client.newActivateJobsCommand()
        .jobType(jobType)
        .maxJobsToActivate(1)
        .send()
        .join();

    assertEquals(jobResponse.getJobs().size(), 1);

    client.newThrowErrorCommand(jobResponse.getJobs().get(0).getKey())
        .errorCode("error-code")
        .errorMessage("error-message")
        .send()
        .join();
  }

}
