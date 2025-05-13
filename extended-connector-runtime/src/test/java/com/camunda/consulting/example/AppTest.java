package com.camunda.consulting.example;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@CamundaSpringProcessTest
@Testcontainers
public class AppTest {
  @Autowired ZeebeClient zeebeClient;

  @Container
  private final GenericContainer<?> httpbin = new GenericContainer<>("kennethreitz/httpbin")
      .withExposedPorts(80);


  @BeforeEach
  public void setup() {
    zeebeClient.newDeployResourceCommand().addResourceFromClasspath("test.bpmn").send().join();
  }

  @Test
  void shouldExecute() {
    Integer mappedPort = httpbin.getMappedPort(80);
    Duration nextExecutionBackoff = Duration.ofMinutes(20);
    ProcessInstanceResult result =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("test")
            .latestVersion()
            .variable("host", "http://localhost:" + mappedPort)
            .withResult()
            .send()
            .join();
    assertThat(result).isNotNull();
    assertThat(Duration.parse((CharSequence) result.getVariable("nextExecutionBackoff")))
        .isEqualTo(nextExecutionBackoff);
  }
}
