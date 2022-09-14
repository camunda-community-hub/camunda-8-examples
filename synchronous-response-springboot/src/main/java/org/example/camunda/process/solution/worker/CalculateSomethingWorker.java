package org.example.camunda.process.solution.worker;

import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public class CalculateSomethingWorker {

  @ZeebeWorker(type = "calculateSomething", autoComplete = true)
  public Map<String, Object> calculateSomething() {
    return Collections.singletonMap("response", "The response is - of course - 42");
  }
}
