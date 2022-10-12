package org.example.camunda.process.solution;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableZeebeClient
@Deployment(resources = "classpath*:/models/*.*")
public class SyncResponseProcessApplication {

  public static void main(String[] args) {
    SpringApplication.run(SyncResponseProcessApplication.class, args);
  }
}
