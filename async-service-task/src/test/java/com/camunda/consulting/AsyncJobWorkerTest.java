package com.camunda.consulting;

import static io.camunda.process.test.api.CamundaAssert.*;

import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@CamundaSpringProcessTest
@SpringBootTest
public class AsyncJobWorkerTest {

  @Autowired ZeebeClient zeebeClient;

  @Test
  void shouldRun() {
    setAssertionTimeout(Duration.ofMinutes(2));
    ProcessInstanceEvent process =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("AsyncServiceTaskProcess")
            .latestVersion()
            .send()
            .join();
    assertThat(process).isCompleted();
  }
}
