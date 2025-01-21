package com.camunda.consulting;

import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.*;
import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import java.time.Duration;
import java.util.List;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@ZeebeSpringTest
@SpringBootTest
public class RollbackTest {

  @Autowired ZeebeClient zeebeClient;

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
    userTaskService.complete(awaitUserTask().getKey(), createVariables(false, false));
    waitForProcessInstanceCompleted(process, Duration.ofSeconds(30));
    BpmnAssert.assertThat(process)
        .hasPassedElement("EnterDataTask", 1)
        .hasPassedElement("ValidateDataTask", 1)
        .hasPassedElement("SaveDataTask", 1);
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
    BpmnAssert.assertThat(process)
        .hasNotPassedElement("ValidateDataTask")
        .hasNotPassedElement("SaveDataTask");
    userTaskService.complete(awaitUserTask().getKey(), createVariables(false, false));
    waitForProcessInstanceCompleted(process, Duration.ofSeconds(30));
    BpmnAssert.assertThat(process)
        .hasPassedElement("EnterDataTask", 2)
        .hasPassedElement("ValidateDataTask", 1)
        .hasPassedElement("SaveDataTask", 1);
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
    BpmnAssert.assertThat(process)
        .hasPassedElement("ValidateDataTask", 1)
        .hasNotPassedElement("SaveDataTask");
    userTaskService.complete(awaitUserTask().getKey(), createVariables(false, false));
    waitForProcessInstanceCompleted(process, Duration.ofSeconds(30));
    BpmnAssert.assertThat(process)
        .hasPassedElement("EnterDataTask", 2)
        .hasPassedElement("ValidateDataTask", 2)
        .hasPassedElement("SaveDataTask", 1);
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
