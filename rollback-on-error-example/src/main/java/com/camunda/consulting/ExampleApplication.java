package com.camunda.consulting;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExampleApplication {
  private static final Logger LOG = LoggerFactory.getLogger(ExampleApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(ExampleApplication.class, args);
  }
}
