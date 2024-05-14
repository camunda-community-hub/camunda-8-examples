package com.camunda.consulting.tasklist.configuration;

import com.camunda.consulting.tasklist.CamundaTasklistProperties;
import io.camunda.common.auth.Authentication;
import io.camunda.common.auth.JwtConfig;
import io.camunda.common.auth.JwtCredential;
import io.camunda.common.auth.Product;
import io.camunda.common.auth.SelfManagedAuthentication;
import io.camunda.common.auth.identity.IdentityConfig;
import io.camunda.common.auth.identity.IdentityContainer;
import io.camunda.identity.sdk.Identity;
import io.camunda.identity.sdk.IdentityConfiguration;
import io.camunda.identity.sdk.IdentityConfiguration.Type;
import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.CamundaTaskListClientBuilder;
import io.camunda.tasklist.exception.TaskListException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaTasklistClientConfiguration {
  @Bean
  public CamundaTaskListClient camundaTaskListClient(
      @Qualifier("camundaTaskListAuth") Authentication authentication, CamundaTasklistProperties properties
  ) throws TaskListException {
    return new CamundaTaskListClientBuilder()
        .taskListUrl(properties.url())
        .shouldReturnVariables()
        .authentication(authentication)
        .build();
  }

  @Bean
  public Authentication camundaTaskListAuth(CamundaTasklistProperties properties) {
    JwtConfig jwtConfig = new JwtConfig();
    jwtConfig.addProduct(Product.TASKLIST,
        new JwtCredential(properties
            .authentication()
            .clientId(),
            properties
                .authentication()
                .clientSecret(),
            properties
                .authentication()
                .audience(),
            properties
                .authentication()
                .issuer()
        )
    );
    IdentityConfiguration identityConfiguration = new IdentityConfiguration(properties
        .authentication()
        .issuer(),
        properties
            .authentication()
            .issuer(),
        properties
            .authentication()
            .clientId(),
        properties
            .authentication()
            .clientSecret(),
        properties
            .authentication()
            .audience(),
        Type.KEYCLOAK.name()
    );
    Identity identity = new Identity(identityConfiguration);
    IdentityContainer identityContainer = new IdentityContainer(identity, identityConfiguration);
    IdentityConfig identityConfig = new IdentityConfig();
    identityConfig.addProduct(Product.TASKLIST, identityContainer);
    return SelfManagedAuthentication
        .builder()
        .withIdentityConfig(identityConfig)
        .withJwtConfig(jwtConfig)
        .build();
  }
}
