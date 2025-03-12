package com.camunda.consulting.example.versioned_job_worker;

import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.model.ProcessDefinition;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ProcessVersionTagResolver {
  private final CamundaOperateClient camundaOperateClient;
  private final Map<Long, String> versionTagForProcessDefinition = new HashMap<>();

  public ProcessVersionTagResolver(CamundaOperateClient camundaOperateClient) {
    this.camundaOperateClient = camundaOperateClient;
  }

  public String versionTagForProcessDefinition(long processDefinitionKey) {
    if (!versionTagForProcessDefinition.containsKey(processDefinitionKey)) {
      ProcessDefinition processDefinition = getProcessDefinition(processDefinitionKey, 10);
      versionTagForProcessDefinition.put(processDefinitionKey, processDefinition.getVersionTag());
    }
    return versionTagForProcessDefinition.get(processDefinitionKey);
  }

  private ProcessDefinition getProcessDefinition(long processDefinitionKey, int retries) {
    try {
      return camundaOperateClient.getProcessDefinition(processDefinitionKey);
    } catch (Exception e) {
      if (retries > 0) {
        try {
          Thread.sleep(1000L);
        } catch (InterruptedException ex) {
          throw new RuntimeException(
              "Interrupted while waiting for retry to fetch process definition", ex);
        }
        return getProcessDefinition(processDefinitionKey, retries - 1);
      }
      throw new RuntimeException("Error while fetching process definition", e);
    }
  }
}
