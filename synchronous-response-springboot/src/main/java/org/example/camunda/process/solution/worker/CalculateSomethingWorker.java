package org.example.camunda.process.solution.worker;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public class CalculateSomethingWorker {
  
  private static final Logger LOG = LoggerFactory.getLogger(CalculateSomethingWorker.class);


  @JobWorker
  public Map<String, Object> calculateSomething() {
    try {
      LOG.info("Cause a 5 seconds delay");
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      LOG.error("Thread sleep got interrupted: {}", e.getMessage());
    }
    return Collections.singletonMap("response", "The response is - of course - 42");
  }
}
