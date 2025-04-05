package com.camunda.consulting.example.versioned_job_worker;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Deployment(resources = "classpath*:*.bpmn")
public class App {
  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }
}
