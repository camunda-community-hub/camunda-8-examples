package com.camunda.consulting;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.process.test.api.CamundaAssert;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@CamundaSpringProcessTest
public class ParallelOperationsAppTest {
  @Autowired CamundaClient camundaClient;
  @Autowired RuntimeService runtimeService;
  @Autowired HistoryService historyService;
  @MockitoBean StringService stringService;

  @Test
  void shouldRun() {
    when(stringService.get()).thenReturn("test");
    ProcessInstanceEvent migratedProcess =
        camundaClient
            .newCreateInstanceCommand()
            .bpmnProcessId("MigratedProcessProcess")
            .latestVersion()
            .send()
            .join();
    CamundaAssert.assertThat(migratedProcess)
        .isCompleted()
        .hasVariable("someText", "test")
        .hasVariable("someOtherText", "testtest")
        .hasVariable("lastText", "testtesttest")
        .hasVariable("callbackId", "test");
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
