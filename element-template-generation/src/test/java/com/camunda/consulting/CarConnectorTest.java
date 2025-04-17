package com.camunda.consulting;

import static org.assertj.core.api.Assertions.*;

import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@CamundaSpringProcessTest
@SpringBootTest(
    properties = {
      "camunda.connector.webhook.enabled=false",
      "camunda.connector.polling.enabled=false",
      "operate.client.enabled=false"
    })
public class CarConnectorTest {
  @Autowired ZeebeClient zeebeClient;

  @BeforeEach
  void setup() {
    zeebeClient.newDeployResourceCommand().addResourceFromClasspath("test.bpmn").send().join();
  }

  @Test
  void shouldRun() {
    ProcessInstanceResult result =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("test")
            .latestVersion()
            .withResult()
            .send()
            .join();
    CarConnectorOutput output = result.getVariablesAsType(ResultType.class).car();
    assertThat(output.make()).isEqualTo("Audi");
    assertThat(output.model()).isEqualTo("A6");
    assertThat(output.gearbox()).isEqualTo("Automatic");
  }
}
