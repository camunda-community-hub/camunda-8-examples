package com.camunda.consulting.connector;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ExampleConnectorConfiguration {
  @Bean
  public ExampleConnector exampleFunction(MyBean myBean) {
    return new ExampleConnector(myBean);
  }

  @Bean
  public MyBean myBean() {
    return new MyBean();
  }
}
