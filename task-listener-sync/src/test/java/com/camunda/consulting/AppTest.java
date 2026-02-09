package com.camunda.consulting;

import static io.camunda.process.test.api.CamundaAssert.*;
import static io.camunda.process.test.api.assertions.ElementSelectors.*;

import com.camunda.consulting.InternalTask.State;
import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.client.api.search.response.SearchResponse;
import io.camunda.client.api.search.response.UserTask;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@CamundaSpringProcessTest
public class AppTest {
  @Autowired CamundaClient camundaClient;

  @Autowired TaskService taskService;

  @BeforeEach
  public void deploy() {
    camundaClient.newDeployResourceCommand().addResourceFromClasspath("user-tasks.bpmn").execute();
  }

  @Test
  void shouldSyncCreatingAndCompleting() {
    ProcessInstanceEvent processInstance =
        camundaClient
            .newCreateInstanceCommand()
            .bpmnProcessId("userTasksProcess")
            .latestVersion()
            .execute();
    assertThat(processInstance).hasActiveElement(byName("Reactive user task"), 1);
    SearchResponse<UserTask> userTaskQuery =
        camundaClient
            .newUserTaskSearchRequest()
            .filter(f -> f.processInstanceKey(processInstance.getProcessInstanceKey()))
            .execute();
    Assertions.assertThat(userTaskQuery.items()).hasSize(1);
    UserTask userTask = userTaskQuery.items().getFirst();
    TaskDto taskDto = taskService.getTask(userTask.getUserTaskKey());
    Assertions.assertThat(taskDto).isNotNull().extracting(TaskDto::state).isEqualTo(State.CREATED);
    camundaClient.newCompleteUserTaskCommand(userTask.getUserTaskKey()).execute();
    assertThat(processInstance).hasCompletedElement(byName("Reactive user task"), 1);
    taskDto = taskService.getTask(userTask.getUserTaskKey());
    Assertions.assertThat(taskDto)
        .isNotNull()
        .extracting(TaskDto::state)
        .isEqualTo(State.COMPLETED);
  }

  @Test
  void shouldPollCreatingAndCompleting() throws InterruptedException {
    ProcessInstanceEvent processInstance =
        camundaClient
            .newCreateInstanceCommand()
            .bpmnProcessId("userTasksProcess")
            .latestVersion()
            .startBeforeElement("PollableUserTaskTask")
            .execute();
    assertThat(processInstance).hasActiveElement(byName("Pollable user task"), 1);
    SearchResponse<UserTask> userTaskQuery =
        camundaClient
            .newUserTaskSearchRequest()
            .filter(f -> f.processInstanceKey(processInstance.getProcessInstanceKey()))
            .execute();
    Assertions.assertThat(userTaskQuery.items()).hasSize(1);
    UserTask userTask = userTaskQuery.items().getFirst();
    Awaitility.await()
        .untilAsserted(
            () ->
                Assertions.assertThat(taskService.getTask(userTask.getUserTaskKey()))
                    .isNotNull()
                    .extracting(TaskDto::state)
                    .isEqualTo(State.CREATED));
    TaskDto taskDto = taskService.getTask(userTask.getUserTaskKey());
    Assertions.assertThat(taskDto).isNotNull().extracting(TaskDto::state).isEqualTo(State.CREATED);
    camundaClient.newCompleteUserTaskCommand(userTask.getUserTaskKey()).execute();
    assertThat(processInstance).hasCompletedElement(byName("Pollable user task"), 1);
    Awaitility.await()
        .untilAsserted(
            () ->
                Assertions.assertThat(taskService.getTask(userTask.getUserTaskKey()))
                    .isNotNull()
                    .extracting(TaskDto::state)
                    .isEqualTo(State.COMPLETED));
  }
}
