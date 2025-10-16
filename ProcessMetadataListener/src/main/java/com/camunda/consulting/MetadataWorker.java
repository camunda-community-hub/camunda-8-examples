package com.camunda.consulting;

import com.camunda.consulting.ProcessMetadataResult.Camunda;
import io.camunda.client.CamundaClient;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.exception.CamundaError;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class MetadataWorker {
  private final CamundaClient camundaClient;
  private final Map<Long, String> versionTagCache = new ConcurrentHashMap<>();

  public MetadataWorker(CamundaClient camundaClient) {
    this.camundaClient = camundaClient;
  }

  @JobWorker
  public ProcessMetadataResult metadata(ActivatedJob job) {
    String versionTag =
        versionTagCache.computeIfAbsent(job.getProcessDefinitionKey(), this::getVersionTag);
    return new ProcessMetadataResult(
        new Camunda(
            String.valueOf(job.getProcessInstanceKey()),
            String.valueOf(job.getProcessDefinitionKey()),
            job.getBpmnProcessId(),
            job.getProcessDefinitionVersion(),
            versionTag));
  }

  private String getVersionTag(Long processDefinitionKey) {
    try {
      return camundaClient
          .newProcessDefinitionGetRequest(processDefinitionKey)
          .execute()
          .getVersionTag();
    } catch (Exception e) {
      throw CamundaError.jobError(
          "Could not get version tag for " + processDefinitionKey,
          null,
          null,
          Duration.ofSeconds(1));
    }
  }
}
