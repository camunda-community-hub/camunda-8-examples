package com.camunda.consulting;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.*;
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
  public void testCampaignProcess() {
    runProcessAndTestCompleted("CampaignProcess", "2");
  }

  @Test
  public void testLargeCampaignProcess() {
    runProcessAndTestCompleted("LargeCampaignProcess", "3");
  }

  @Test
  public void testHugeCampaignWithFewElements() {
    runProcessAndTestCompleted("HugeCampaignProcess", "1");
  }

  protected void runProcessAndTestCompleted(String processId, String campaignId) {
    ProcessInstanceEvent processInstance =
        client
            .newCreateInstanceCommand()
            .bpmnProcessId(processId)
            .latestVersion()
            .variables(Map.of("campaignId", campaignId))
            .send()
            .join();

    waitForProcessInstanceCompleted(processInstance, Duration.ofMinutes(1));

    assertThat(processInstance).isCompleted();
  }
}
