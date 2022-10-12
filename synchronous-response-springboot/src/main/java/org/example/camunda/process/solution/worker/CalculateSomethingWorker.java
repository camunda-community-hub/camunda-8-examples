package org.example.camunda.process.solution.worker;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public class CalculateSomethingWorker {

  @JobWorker
  public Map<String, Object> calculateSomething() {
    return Collections.singletonMap("response", "The response is - of course - 42");
  }
}
