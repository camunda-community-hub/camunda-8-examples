package com.camunda.consulting;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.camunda.process.test.api.CamundaAssert.*;
import static org.mockito.Mockito.*;

import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.zeebe.client.CredentialsProvider;
import io.camunda.zeebe.client.CredentialsProvider.CredentialsApplier;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.wiremock.spring.EnableWireMock;

@CamundaSpringProcessTest
@SpringBootTest
@EnableWireMock
public class AppTest {
  @Autowired ZeebeClient zeebeClient;
  @MockitoBean CredentialsProvider credentialsProvider;

  @Value("${wiremock.server.baseUrl}")
  private String wireMockUrl;

  @Test
  void shouldRunProcessInstance() throws IOException {
    doAnswer(
            inv -> {
              CredentialsApplier applier = inv.getArgument(0);
              applier.put("Authorization", "Bearer xyz");
              return null;
            })
        .when(credentialsProvider)
        .applyCredentials(any());
    stubFor(
        get("/v2/topology").withHeader("Authorization", equalTo("Bearer xyz")).willReturn(ok("x")));
    zeebeClient.newDeployResourceCommand().addResourceFromClasspath("test.bpmn").send().join();
    ProcessInstanceEvent processInstanceEvent =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("ZeebeTopologyFetcherProcess")
            .latestVersion()
            .variable("zeebeTopologyUrl", wireMockUrl + "/v2/topology")
            .send()
            .join();
    assertThat(processInstanceEvent).isCompleted().hasVariable("topology", "x");
  }
}
