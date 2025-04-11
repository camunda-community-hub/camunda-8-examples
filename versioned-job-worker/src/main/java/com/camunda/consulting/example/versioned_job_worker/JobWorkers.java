package com.camunda.consulting.example.versioned_job_worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JobWorkers {
  private static final Logger LOG = LoggerFactory.getLogger(JobWorkers.class);
  private final ProcessVersionTagResolver processVersionTagResolver;

  public JobWorkers(ProcessVersionTagResolver processVersionTagResolver) {
    this.processVersionTagResolver = processVersionTagResolver;
  }

  @JobWorker
  public void doStuff(ActivatedJob job) {
    // resolve version tag (currently requires a workaround)
    String versionTag =
        processVersionTagResolver.versionTagForProcessDefinition(job.getProcessDefinitionKey());
    switch (versionTag) {
      case "V1" -> doStuffV1();
      case "V2" -> doStuffV2();
      case null, default -> throw new RuntimeException("Unknown version tag: " + versionTag);
    }
  }

  private void doStuffV1() {
    LOG.info("Doing stuff v1");
  }

  private void doStuffV2() {
    LOG.info("Doing stuff v2");
  }
}
