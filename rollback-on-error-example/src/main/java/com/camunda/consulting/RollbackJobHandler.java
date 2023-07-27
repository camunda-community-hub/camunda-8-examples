package com.camunda.consulting;

import static com.camunda.consulting.UserTaskService.*;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import java.util.Collections;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RollbackJobHandler implements JobHandler {
  private static final Logger LOG = LoggerFactory.getLogger(RollbackJobHandler.class);
  private final DynamicJobHandler jobHandler;
  private final String rollbackElementId;
  private final String originalKeyVariableName;
  private final long originalKey;
  private final Consumer<Exception> exceptionHandler;
  private final ZeebeClient zeebeClient;
  private Runnable closer;

  public RollbackJobHandler(
      DynamicJobHandler jobHandler,
      String rollbackElementId, String originalKeyVariableName, long originalKey,
      Consumer<Exception> exceptionHandler,
      ZeebeClient zeebeClient) {
    this.jobHandler = jobHandler;
    this.rollbackElementId = rollbackElementId;
    this.originalKeyVariableName = originalKeyVariableName;
    this.originalKey = originalKey;
    this.exceptionHandler = exceptionHandler;
    this.zeebeClient = zeebeClient;
  }

  @Override
  public void handle(JobClient client, ActivatedJob job) throws Exception {
    try {
      Object result = jobHandler.handle(job);
      if(result == null){
        result = JsonNodeFactory.instance.objectNode();
      }
      client.newCompleteCommand(job).variables(result).send().join();
      LOG.info("Completed job {} successfully", job.getKey());
    } catch (Exception e) {
      LOG.error(
          "Job completion for {} failed, rolling back to {}", job.getKey(), rollbackElementId);
      LOG.error("Original Exception:",e);
      zeebeClient
          .newModifyProcessInstanceCommand(job.getProcessInstanceKey())
          .activateElement(rollbackElementId)
          .withVariables(Collections.singletonMap(originalKeyVariableName, originalKey))
          .and()
          .terminateElement(job.getElementInstanceKey())
          .send()
          .join();
      exceptionHandler.accept(e);
    } finally {
      LOG.info("Closing job handlers");
      closer.run();
    }
  }

  public void setCloser(Runnable closer) {
    this.closer = closer;
  }
}
