package com.camunda.consulting.web_shop_process_app;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Deployment(resources = "payment_process.bpmn")
public class PaymentProcessApplication {

  public static void main(String[] args) {
    SpringApplication.run(PaymentProcessApplication.class, args);
  }
}
