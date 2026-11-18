package com.camunda.consulting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.camunda.consulting.impl.CompletedTaskMessage;
import com.camunda.consulting.listener.CannotCompleteTaskException;
import com.camunda.consulting.listener.CompletedTaskListener;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivateJobsResponse;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;

@ZeebeProcessTest
public class CompleteTaskListenerTest {

  private ZeebeTestEngine engine;
  private ZeebeClient client;


  @Test
  public void whenCompleteReceivedCompleteUserTask()
      throws InterruptedException, TimeoutException, CannotCompleteTaskException {

    // Given
    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
        .startEvent()
        .userTask("userTask")
        .endEvent()
        .done();

    client.newDeployResourceCommand()
        .addProcessModel(process, "process.bpmn")
        .send()
        .join();

    ProcessInstanceEvent event = client.newCreateInstanceCommand()
        .bpmnProcessId("process")
        .latestVersion()
        .send()
        .join();

    engine.waitForIdleState(Duration.ofSeconds(1));

    ProcessInstanceAssert instance = BpmnAssert.assertThat(event);

    instance.isWaitingAtElements("userTask");

    ActivateJobsResponse response = client.newActivateJobsCommand()
        .jobType("io.camunda.zeebe:userTask")
        .maxJobsToActivate(1)
        .send()
        .join();

    assertEquals(1, response.getJobs().size());

    ActivatedJob activatedJob = response.getJobs().get(0);
    long jobKey = activatedJob.getKey();
    CompletedTaskMessage completedTaskMessage = new CompletedTaskMessage();
    completedTaskMessage.setId(String.valueOf(jobKey));
    completedTaskMessage.setVariables(Map.of("test", "test"));

    // When
    CompletedTaskListener listener = new CompletedTaskListener(client);
    listener.listen(completedTaskMessage);
    engine.waitForIdleState(Duration.ofSeconds(1));

    // Then
    instance.isCompleted();
    instance.hasVariableWithValue("test", "test");
  }

  @Test
  public void whenCompleteTaskThatDoesNotExistThrow() {
    // Given
    CompletedTaskMessage completedTaskMessage = new CompletedTaskMessage();
    completedTaskMessage.setId("123");
    completedTaskMessage.setVariables(Map.of("test", "test"));

    // When
    CompletedTaskListener listener = new CompletedTaskListener(client);

    // Then
    assertThrows(CannotCompleteTaskException.class, () -> listener.listen(completedTaskMessage));
  }

}
