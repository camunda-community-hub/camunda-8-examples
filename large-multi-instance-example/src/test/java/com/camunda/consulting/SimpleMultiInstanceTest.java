package com.camunda.consulting;

import static io.camunda.process.test.api.CamundaAssert.*;

import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@CamundaSpringProcessTest
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

    assertThat(processInstance).hasVariable("resultList", List.of(65, 66, 67));
  }
}
