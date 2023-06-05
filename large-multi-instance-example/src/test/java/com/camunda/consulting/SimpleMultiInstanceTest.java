package com.camunda.consulting;

import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.*;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import java.time.Duration;
import java.util.List;
import org.camunda.community.process_test_coverage.junit5.platform8.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ZeebeSpringTest
@ExtendWith(ProcessEngineCoverageExtension.class)
public class SimpleMultiInstanceTest {

  private static final Logger LOG = LoggerFactory.getLogger(SimpleMultiInstanceTest.class);

  @Autowired ZeebeClient client;

  @Test
  public void runCallActivity() {
    LOG.info("Call activity");
    ProcessInstanceEvent processInstance =
        client
            .newCreateInstanceCommand()
            .bpmnProcessId("MICallActivityProcess")
            .latestVersion()
            .send()
            .join();

    waitForProcessInstanceCompleted(processInstance, Duration.ofMinutes(1));
    BpmnAssert.assertThat(processInstance).hasVariableWithValue("resultList", List.of(65, 66, 67));
  }
}
