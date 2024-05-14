package com.camunda.consulting.tasklist;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("camunda.webhook")
public record CamundaWebhookProperties(String url, Authorization authorization) {
  public record Authorization(String username, String password) {}
}
