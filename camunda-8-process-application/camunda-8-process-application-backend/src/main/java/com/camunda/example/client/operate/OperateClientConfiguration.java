package com.camunda.example.client.operate;

import com.camunda.example.client.operate.model.*;
import com.fasterxml.jackson.databind.*;
import io.camunda.zeebe.client.*;
import io.camunda.zeebe.client.impl.oauth.*;
import io.camunda.zeebe.spring.client.properties.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.http.client.*;
import org.springframework.web.client.*;

import java.net.*;

@Configuration
public class OperateClientConfiguration {

  @Bean
  @Qualifier("operate-url")
  public URI operateEndpoint(ZeebeClientConfigurationProperties properties) {
    return URI.create("https://" + properties
        .getCloud()
        .getRegion() + ".operate.camunda.io/" + properties
        .getCloud()
        .getClusterId() + "/v1");
  }

  @Bean
  @Qualifier("operate-credentials-provider")
  public CredentialsProvider operateCredentialsProvider(ZeebeClientConfigurationProperties properties) {
    return new OAuthCredentialsProviderBuilder()
        .clientId(properties
            .getCloud()
            .getClientId())
        .clientSecret(properties
            .getCloud()
            .getClientSecret())
        .audience("operate.camunda.io")
        .build();
  }

  @Bean
  @Qualifier("operate-rest-template")
  public RestTemplate operateRestTemplate(ObjectMapper objectMapper) {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    restTemplate.setErrorHandler(new OperateResponseErrorHandler(objectMapper));
    return restTemplate;
  }

  @Bean
  public OperateProcessDefinitionsEndpoint processDefinitionsEndpoint(
      @Qualifier("operate-rest-template") RestTemplate restTemplate,
      @Qualifier("operate-url") URI operateEndpoint,
      @Qualifier("operate-credentials-provider") CredentialsProvider credentialsProvider,
      ObjectMapper objectMapper
  ) {
    return new OperateProcessDefinitionsEndpoint(restTemplate, operateEndpoint, credentialsProvider, objectMapper);
  }

  @Bean
  public OperateProcessInstancesEndpoint processInstancesEndpoint(
      @Qualifier("operate-rest-template") RestTemplate restTemplate,
      @Qualifier("operate-url") URI operateEndpoint,
      @Qualifier("operate-credentials-provider") CredentialsProvider credentialsProvider,
      ObjectMapper objectMapper
  ) {
    return new OperateProcessInstancesEndpoint(restTemplate, operateEndpoint, credentialsProvider, objectMapper);
  }

  @Bean
  public OperateIncidentsEndpoint incidentsEndpoint(
      @Qualifier("operate-rest-template") RestTemplate restTemplate,
      @Qualifier("operate-url") URI operateEndpoint,
      @Qualifier("operate-credentials-provider") CredentialsProvider credentialsProvider,
      ObjectMapper objectMapper
  ) {
    return new OperateIncidentsEndpoint(restTemplate, operateEndpoint, credentialsProvider, objectMapper);
  }

  @Bean
  public OperateFlowNodeInstancesEndpoint flowNodeInstancesEndpoint(
      @Qualifier("operate-rest-template") RestTemplate restTemplate,
      @Qualifier("operate-url") URI operateEndpoint,
      @Qualifier("operate-credentials-provider") CredentialsProvider credentialsProvider,
      ObjectMapper objectMapper
  ) {
    return new OperateFlowNodeInstancesEndpoint(restTemplate, operateEndpoint, credentialsProvider, objectMapper);
  }

  @Bean
  public OperateVariablesEndpoint variablesEndpoint(
      @Qualifier("operate-rest-template") RestTemplate restTemplate,
      @Qualifier("operate-url") URI operateEndpoint,
      @Qualifier("operate-credentials-provider") CredentialsProvider credentialsProvider,
      ObjectMapper objectMapper
  ) {
    return new OperateVariablesEndpoint(restTemplate, operateEndpoint, credentialsProvider, objectMapper);
  }
}
