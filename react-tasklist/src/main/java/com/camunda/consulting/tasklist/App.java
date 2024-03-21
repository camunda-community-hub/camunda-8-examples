package com.camunda.consulting.tasklist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({CamundaTasklistProperties.class, CamundaWebhookProperties.class})
public class App {
  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }
}
