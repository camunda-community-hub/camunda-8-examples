package com.camunda.consulting;

import static io.camunda.process.test.api.CamundaAssert.*;

import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@CamundaSpringProcessTest
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
    assertThat(processInstance).isCompleted();
  }
}
