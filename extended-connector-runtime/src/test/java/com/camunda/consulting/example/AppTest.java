package com.camunda.consulting.example;

import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@CamundaSpringProcessTest
public class AppTest {
  @Autowired
  ZeebeClient zeebeClient;

  @MockitoBean
  SchedulingService schedulingService;

  @BeforeEach
  public void setup() {
    zeebeClient.newDeployResourceCommand().addResourceFromClasspath("test.bpmn").send().join();
  }

  @Test
  void shouldExecute() {
    ZonedDateTime now = ZonedDateTime.now();
    when(schedulingService.schedule(any())).thenReturn(now);
    ProcessInstanceResult result = zeebeClient
        .newCreateInstanceCommand()
        .bpmnProcessId("test")
        .latestVersion()
        .withResult()
        .send()
        .join();
    verify(schedulingService,times(1)).schedule(any());
    assertThat(result).isNotNull();
    assertThat(ZonedDateTime.parse((CharSequence) result.getVariable("nextExecutionTimeslot"))).isEqualTo(now);
  }
}
