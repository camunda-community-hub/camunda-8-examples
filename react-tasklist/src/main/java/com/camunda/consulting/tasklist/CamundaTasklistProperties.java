package com.camunda.consulting.tasklist;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("camunda.tasklist")
public record CamundaTasklistProperties(String url,Authentication authentication) {
  public record Authentication(String clientId,String clientSecret,String issuer, String audience){}
}
