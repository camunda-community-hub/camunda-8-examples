package com.camunda.example;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;

@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(resources = { "classpath*:**.bpmn", "classpath*:**.dmn" })
public class CloudTasklistApplication {
  public static void main(String[] args) {
    SpringApplication.run(CloudTasklistApplication.class, args);
  }

  @EnableWebSecurity
  public static class OAuth2ResourceServerSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http.csrf().disable();
      http.authorizeRequests().anyRequest().authenticated();
      http.oauth2Login();
      http.oauth2Client();
      http.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
    }
  }
}
