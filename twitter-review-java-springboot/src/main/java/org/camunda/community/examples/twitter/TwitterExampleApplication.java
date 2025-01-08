package org.camunda.community.examples.twitter;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Deployment(resources = "classpath*:*.bpmn")
public class TwitterExampleApplication {

  public static void main(String[] args) {
    SpringApplication.run(TwitterExampleApplication.class, args);
  }
}
