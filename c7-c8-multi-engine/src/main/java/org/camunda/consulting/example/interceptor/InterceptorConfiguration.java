package org.camunda.consulting.example.interceptor;

import io.grpc.ClientInterceptor;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "optimize.interceptor.enabled", havingValue = "true")
public class InterceptorConfiguration {

  @Value("${optimize.interceptor.ingestionEndpoint}")
  private String optimizeUrl;
  @Value("${optimize.interceptor.token}")
  private String optimizeToken;

  @Bean
  public List<ClientInterceptor>  clientInterceptors() {
    return List.of(new OptimizeEventInterceptor(optimizeUrl, optimizeToken));
  }

}
