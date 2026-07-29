package com.camunda.consulting;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class RetryableJobWorker {
  private static final Logger LOG = LoggerFactory.getLogger(RetryableJobWorker.class);

  @JobWorker(autoComplete = false)
  public void sendMessage(
      @Variable Integer retryCounter, @Variable String callbackId, JobClient jobClient, ActivatedJob job
  ) {
    LOG.info("retryCounter is {}", retryCounter);
    if (retryCounter != null && retryCounter < 1) {
      jobClient
          .newFailCommand(job)
          .retries(0)
          .errorMessage("No retries left")
          .variable("retryCounter", 1)
          .send()
          .join();
      return;
    }
    if (callbackId == null) {
      callbackId = UUID
          .randomUUID()
          .toString();
    }
    LOG.info("Setting callbackId {}", callbackId);
    sendMessage();
    int nextRetryCounter = Optional
        .ofNullable(retryCounter)
        .map(i -> i - 1)
        .orElse(2);
    LOG.info("Setting retryCounter {}", nextRetryCounter);
    jobClient
        .newCompleteCommand(job.getKey())
        .variables(Map.of("retryCounter", nextRetryCounter, "callbackId", callbackId))
        .send()
        .join();
  }

  private void sendMessage() {
    LOG.info("message sent");
  }

}
