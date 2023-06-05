package com.camunda.consulting;

import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.*;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import java.time.Duration;
import java.util.Map;
import org.camunda.community.process_test_coverage.junit5.platform8.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ZeebeSpringTest
@ExtendWith(ProcessEngineCoverageExtension.class)
public class MultiInstanceTest {

  @Autowired ZeebeClient client;

  @Test
  public void testCampaignWithFewElements() {
    ProcessInstanceEvent processInstance =
        client
            .newCreateInstanceCommand()
            .bpmnProcessId("campaignProcess")
            .latestVersion()
            .variables(Map.of("campaignId", "1"))
            .send()
            .join();

    waitForProcessInstanceCompleted(processInstance, Duration.ofMinutes(1));
  }
}
