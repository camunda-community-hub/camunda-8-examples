package com.camunda.consulting;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Deployment(resources = "classpath*:**/*.bpmn")
public class ExampleApplication {
  private static final Logger LOG = LoggerFactory.getLogger(ExampleApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(ExampleApplication.class, args);
  }

  @JobWorker
  public void callKids(@Variable String message) {
    LOG.info("Message: '{}'", message);
  }

  @JobWorker
  public Map<String, Object> checkProgress(@Variable List<String> options) {
    String progress = options.get(new Random().nextInt(options.size()));
    return Map.of("progress", progress);
  }
}
