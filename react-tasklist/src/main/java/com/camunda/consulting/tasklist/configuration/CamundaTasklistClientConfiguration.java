package com.camunda.consulting.tasklist.configuration;

import com.camunda.consulting.tasklist.CamundaTasklistProperties;
import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.auth.AuthInterface;
import io.camunda.tasklist.auth.SelfManagedAuthentication;
import io.camunda.tasklist.exception.TaskListException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaTasklistClientConfiguration {
  @Bean
  public CamundaTaskListClient camundaTaskListClient(
      AuthInterface authInterface, CamundaTasklistProperties properties) throws TaskListException {
    return new CamundaTaskListClient.Builder()
        .taskListUrl(properties.url())
        .shouldReturnVariables()
        .authentication(authInterface)
        .build();
  }

  @Bean
  public AuthInterface camundaTaskListAuth(CamundaTasklistProperties properties) {
    return new SelfManagedAuthentication()
        .clientId(properties.authentication().clientId())
        .clientSecret(properties.authentication().clientSecret())
        .keycloakUrl(properties.authentication().keycloakUrl())
        .keycloakRealm(properties.authentication().keycloakRealm());
  }
}
