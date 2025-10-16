package com.camunda.consulting;

import static org.assertj.core.api.Assertions.*;

import com.camunda.consulting.ProcessMetadataResult.Camunda;
import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.DeploymentEvent;
import io.camunda.client.api.response.Process;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.process.test.api.CamundaAssert;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@CamundaSpringProcessTest
public class MetadataWorkerTest {
  @Autowired CamundaClient camundaClient;

  @Test
  public void shouldSetProcessInstanceMetadata() {
    DeploymentEvent deploymentEvent =
        camundaClient.newDeployResourceCommand().addResourceFromClasspath("test.bpmn").execute();
    assertThat(deploymentEvent.getProcesses()).hasSize(1);
    Process process = deploymentEvent.getProcesses().get(0);
    ProcessInstanceEvent processInstance =
        camundaClient
            .newCreateInstanceCommand()
            .processDefinitionKey(process.getProcessDefinitionKey())
            .execute();
    CamundaAssert.assertThat(processInstance)
        .isCompleted()
        .hasVariableSatisfies(
            "camunda",
            Camunda.class,
            camunda -> {
              assertThat(camunda.processDefinitionId()).isEqualTo(process.getBpmnProcessId());
              assertThat(camunda.processDefinitionKey())
                  .isEqualTo(String.valueOf(process.getProcessDefinitionKey()));
              assertThat(camunda.processInstanceKey())
                  .isEqualTo(String.valueOf(processInstance.getProcessInstanceKey()));
              assertThat(camunda.version()).isEqualTo(process.getVersion());
            });
  }
}
