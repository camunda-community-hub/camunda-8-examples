package com.camunda.consulting;

import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.process.test.api.CamundaAssert;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import java.time.Duration;
import java.util.List;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@CamundaSpringProcessTest
@SpringBootTest
public class RollbackTest {

  @Autowired CamundaClient zeebeClient;

  @Autowired UserTaskService userTaskService;

  @Test
  void shouldRun() {
    ProcessInstanceEvent process =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("RollbackOnErrorProcess")
            .latestVersion()
            .send()
            .join();
    userTaskService.complete(awaitUserTask().getKey(), JsonNodeFactory.instance.objectNode());
    CamundaAssert.assertThat(process)
        .isCompleted()
        .hasCompletedElement("EnterDataTask", 1)
        .hasCompletedElement("ValidateDataTask", 1)
        .hasCompletedElement("SaveDataTask", 1);
  }

  @Test
  void shouldFailOnValidation() {
    ProcessInstanceEvent process =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("RollbackOnErrorProcess")
            .latestVersion()
            .send()
            .join();
    assertThatThrownBy(
            () -> userTaskService.complete(awaitUserTask().getKey(), createVariables(true, false)))
        .isInstanceOf(RuntimeException.class);
    awaitUserTask();
    CamundaAssert.assertThat(process).hasNotActivatedElements("SaveDataTask");
    userTaskService.complete(awaitUserTask().getKey(), createVariables(false, false));
    CamundaAssert.assertThat(process)
        .isCompleted()
        .hasCompletedElement("EnterDataTask", 2)
        .hasCompletedElement("ValidateDataTask", 1)
        .hasCompletedElement("SaveDataTask", 1);
  }

  @Test
  void shouldFailOnSaving() {
    ProcessInstanceEvent process =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("RollbackOnErrorProcess")
            .latestVersion()
            .send()
            .join();
    assertThatThrownBy(
            () -> userTaskService.complete(awaitUserTask().getKey(), createVariables(false, true)))
        .isInstanceOf(RuntimeException.class);
    awaitUserTask();
    CamundaAssert.assertThat(process).hasCompletedElement("ValidateDataTask", 1);
    userTaskService.complete(awaitUserTask().getKey(), createVariables(false, false));
    CamundaAssert.assertThat(process)
        .isCompleted()
        .hasCompletedElement("EnterDataTask", 2)
        .hasCompletedElement("ValidateDataTask", 2)
        .hasCompletedElement("SaveDataTask", 1);
  }

  private UserTask awaitUserTask() {
    List<UserTask> availableTasks =
        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .until(() -> userTaskService.getUserTasks(), list -> list.size() == 1);
    return availableTasks.get(0);
  }

  private ObjectNode createVariables(boolean validationFails, boolean savingFails) {
    return JsonNodeFactory.instance
        .objectNode()
        .put("validationFails", validationFails)
        .put("savingFails", savingFails);
  }
}
