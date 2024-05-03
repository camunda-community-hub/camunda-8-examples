package com.camunda.consulting;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableProcessApplication
@Deployment(resources = "classpath*:bpmn/camunda8/*.bpmn")
public class ParallelOperationsApp {
  public static void main(String[] args) {
    SpringApplication.run(ParallelOperationsApp.class, args);
  }
}
