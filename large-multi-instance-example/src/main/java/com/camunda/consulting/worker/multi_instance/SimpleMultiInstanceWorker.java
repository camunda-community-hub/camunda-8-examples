package com.camunda.consulting.worker.multi_instance;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SimpleMultiInstanceWorker {

  private static final Logger LOG = LoggerFactory.getLogger(SimpleMultiInstanceWorker.class);

  @JobWorker
  public Map<String, Object> sequenceCreation(JobClient client, ActivatedJob job) {
    LOG.info("Create Sequence");
    Map<String, Object> outputVars = new HashMap<>();
    outputVars.put("sequence", List.of("A", "B", "C"));
    return outputVars;
  }

  @JobWorker
  public void resultLogging(JobClient client, ActivatedJob job) {
    Map<String, Object> inputVars = job.getVariablesAsMap();
    LOG.info("All variables: {}", inputVars);
  }

  @JobWorker
  public Map<String, Object> elementCreation(@Variable String element) {
    LOG.info("Creating element: {}", element);
    return Map.of("result", (int) element.charAt(0));
  }
}
