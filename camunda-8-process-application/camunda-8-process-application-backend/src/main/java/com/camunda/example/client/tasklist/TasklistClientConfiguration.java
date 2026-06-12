package com.camunda.example.client.tasklist;

import io.camunda.zeebe.client.CredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;
import io.camunda.zeebe.spring.client.properties.ZeebeClientConfigurationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.*;
import org.springframework.web.client.*;

import java.net.URI;

@Configuration
public class TasklistClientConfiguration {

  @Bean
  @Qualifier("tasklist-url")
  public URI tasklistUrl(ZeebeClientConfigurationProperties properties) {
    return URI.create("https://" + properties
        .getCloud()
        .getRegion() + ".tasklist.camunda.io/" + properties
        .getCloud()
        .getClusterId() + "/graphql");
  }

  @Bean
  @Qualifier("tasklist-credentials-provider")
  public CredentialsProvider tasklistCredentialsProvider(ZeebeClientConfigurationProperties properties){
    return new OAuthCredentialsProviderBuilder()
        .clientId(properties
            .getCloud()
            .getClientId())
        .clientSecret(properties
            .getCloud()
            .getClientSecret())
        .audience("tasklist.camunda.io")
        .build();
  }

  @Bean
  @Qualifier("tasklist-rest-template")
  public RestTemplate tasklistRestTemplate() {
    return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
  }
}
