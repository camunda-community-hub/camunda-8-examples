package com.camunda.consulting;

import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.*;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@ZeebeSpringTest
@SpringBootTest
public class AsyncJobWorkerTest {

  @Autowired ZeebeClient zeebeClient;

  @Test
  void shouldRun() {
    ProcessInstanceEvent process =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("AsyncServiceTaskProcess")
            .latestVersion()
            .send()
            .join();
    waitForProcessInstanceCompleted(process, Duration.ofMinutes(2));
  }
}
