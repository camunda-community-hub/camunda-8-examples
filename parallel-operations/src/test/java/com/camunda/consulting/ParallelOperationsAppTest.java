package com.camunda.consulting;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.*;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import java.time.Duration;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@ZeebeSpringTest
public class ParallelOperationsAppTest {
  @Autowired ZeebeClient zeebeClient;
  @Autowired RuntimeService runtimeService;
  @Autowired HistoryService historyService;
  @MockBean StringService stringService;

  @Test
  void shouldRun() {
    when(stringService.get()).thenReturn("test");
    ProcessInstanceEvent migratedProcess =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("MigratedProcessProcess")
            .latestVersion()
            .send()
            .join();
    waitForProcessInstanceCompleted(migratedProcess, Duration.ofSeconds(30));
    assertThat(migratedProcess)
        .hasVariableWithValue("someText", "test")
        .hasVariableWithValue("someOtherText", "testtest")
        .hasVariableWithValue("lastText", "testtesttest")
        .hasVariableWithValue("callbackId", "test");
    HistoricProcessInstance test =
        historyService
            .createHistoricProcessInstanceQuery()
            .processInstanceBusinessKey("test")
            .singleResult();
    assertThat(test).isNotNull();
    assertThat(variableInstance(test.getId(), "someText").getValue()).isEqualTo("test");
    assertThat(variableInstance(test.getId(), "someOtherText").getValue()).isEqualTo("testtest");
    assertThat(variableInstance(test.getId(), "lastText").getValue()).isEqualTo("testtesttest");
    assertThat(variableInstance(test.getId(), "callbackId").getValue()).isEqualTo("test");
  }

  private HistoricVariableInstance variableInstance(String processInstanceId, String variableName) {
    return historyService
        .createHistoricVariableInstanceQuery()
        .processInstanceId(processInstanceId)
        .variableName(variableName)
        .singleResult();
  }
}
