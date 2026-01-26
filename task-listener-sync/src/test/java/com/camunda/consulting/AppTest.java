package com.camunda.consulting;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.client.api.search.response.SearchResponse;
import io.camunda.client.api.search.response.UserTask;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static io.camunda.process.test.api.CamundaAssert.*;
import static io.camunda.process.test.api.assertions.ElementSelectors.*;

@SpringBootTest
@CamundaSpringProcessTest
public class AppTest {
  @Autowired
  CamundaClient camundaClient;

  @Autowired
  TaskService taskService;

  @BeforeEach
  public void deploy() {
    camundaClient
        .newDeployResourceCommand()
        .addResourceFromClasspath("user-tasks.bpmn")
        .execute();
  }

  @Test
  void shouldRun() {
    ProcessInstanceEvent processInstance = camundaClient
        .newCreateInstanceCommand()
        .bpmnProcessId("userTasksProcess")
        .latestVersion()
        .execute();
    assertThat(processInstance).hasActiveElement(byName("Reactive user task"), 1);
    SearchResponse<UserTask> userTaskQuery = camundaClient
        .newUserTaskSearchRequest()
        .filter(f -> f.processInstanceKey(processInstance.getProcessInstanceKey()))
        .execute();
    Assertions.assertThat(userTaskQuery.items()).hasSize(1);
    TaskDto taskDto = taskService.getTask(userTaskQuery
        .items()
        .getFirst()
        .getUserTaskKey());
    Assertions.assertThat(taskDto).isNotNull();
  }
}
