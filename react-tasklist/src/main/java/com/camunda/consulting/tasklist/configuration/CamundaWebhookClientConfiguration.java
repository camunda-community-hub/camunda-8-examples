package com.camunda.consulting.tasklist.configuration;

import com.camunda.consulting.tasklist.CamundaWebhookProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class CamundaWebhookClientConfiguration {
  @Bean
  @Qualifier("camundaWebhookClient")
  public RestTemplate camundaWebhookClient(CamundaWebhookProperties properties) {
    var camundaWebhookClient = new RestTemplate();
    var uriTemplateHandler = new DefaultUriBuilderFactory(properties.url());
    camundaWebhookClient
        .getInterceptors()
        .add(
            new BasicAuthenticationInterceptor(
                properties.authorization().username(), properties.authorization().password()));
    camundaWebhookClient.setUriTemplateHandler(uriTemplateHandler);
    return camundaWebhookClient;
  }
}
