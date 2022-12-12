package org.camunda.community.examples;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableZeebeClient
@Deployment(resources = "classpath*:/model/*.*")
public class StartFormProcessApplication {

  public static void main(String[] args) {
    SpringApplication.run(StartFormProcessApplication.class, args);
  }
}
