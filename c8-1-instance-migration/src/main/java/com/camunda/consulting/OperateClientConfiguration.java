package com.camunda.consulting;

import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.auth.AuthInterface;
import io.camunda.operate.auth.SaasAuthentication;
import io.camunda.operate.auth.SelfManagedAuthentication;
import io.camunda.operate.auth.SimpleAuthentication;
import io.camunda.operate.exception.OperateException;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
@Deployment(resources = {"classpath:c8-to-c8-instance_migration.bpmn"})
public class OperateClientConfiguration {
  @Value("${operate.keycloak.url:#{null}}")
  String keycloakUrl;
  @Value("${zeebe.client.cloud.client-id:#{null}}")
  String clientId;

  @Value("${zeebe.client.cloud.client-secret:#{null}}")
  String clientSecret;

  @Value("${operate.baseUrl:operate.camunda.io}")
  String baseUrl;

  @Value("${operate.authUrl:https://login.cloud.camunda.io/oauth/token}")
  String authUrl;

  @Value("${operate.url:http://localhost:8081}")
  String operateUrl;

  @Value("${operate.user:demo}")
  String operateUsr;

  @Value("${operate.password:demo}")
  String password;

  @Value("${operate.keycloak.realm:camunda-platform}")
  String keycloakRealm;

  @Bean
  public CamundaOperateClient operate() {
    AuthInterface auth = null;
    if (Objects.nonNull(clientSecret) && Objects.nonNull(clientId)) {
      if (Objects.nonNull(keycloakUrl)) {
        auth = new SelfManagedAuthentication()
            .clientId(clientId)
            .clientSecret(clientSecret)
            .keycloakRealm(keycloakRealm)
            .keycloakUrl(keycloakUrl);
      } else {
        auth = new SaasAuthentication(authUrl, baseUrl, clientId, clientSecret);
      }
    } else {
      auth = new SimpleAuthentication(operateUsr, password, operateUrl);
    }
    CamundaOperateClient client = null;
    try {
      client = new CamundaOperateClient.Builder().operateUrl(operateUrl)
          .authentication(auth)
          .build();
    } catch (OperateException e) {
      throw new RuntimeException(e);
    }
    return client;
  }
}
