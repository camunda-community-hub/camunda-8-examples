package com.camunda.consulting.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class App {
  public static ApplicationContext applicationContext;

  public static void main(String[] args) {
    applicationContext = SpringApplication.run(App.class, args);
  }

  public static ApplicationContext getApplicationContext() {
    return applicationContext;
  }
}
