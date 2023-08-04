package com.camunda.consulting;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Deployment(resources = "classpath*:*.bpmn")
public class ExampleApplication {
  private static final Logger LOG = LoggerFactory.getLogger(ExampleApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(ExampleApplication.class, args);
  }

  @JobWorker
  public void validateDataType(@Variable Boolean validationFails) {
    LOG.info("Validating data");
    if (validationFails != null && validationFails) {
      throw new RuntimeException("Validation failed");
    }
  }

  @JobWorker
  public void saveDataType(@Variable Boolean savingFails) {
    LOG.info("Saving data");
    if (savingFails != null && savingFails) {
      throw new RuntimeException("Saving failed");
    }
  }
}
