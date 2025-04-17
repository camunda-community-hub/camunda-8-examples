package com.camunda.consulting;

import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * This component collects all instances of {@link BearerTokenProvider}.
 *
 * <p>In this example, only {@link ZeebeBearerTokenProvider} exists.
 *
 * <p>You can create your own beans of type {@link BearerTokenProvider} and they will be
 * automatically added here.
 */
@Component
public class BearerTokenProviderRegistry {
  private final Set<BearerTokenProvider> bearerTokenProviders;

  public BearerTokenProviderRegistry(Set<BearerTokenProvider> bearerTokenProviders) {
    this.bearerTokenProviders = bearerTokenProviders;
  }

  public String getBearerTokenForService(String serviceName) {
    return bearerTokenProviders.stream()
        .filter(bearerTokenProvider -> bearerTokenProvider.getServiceName().equals(serviceName))
        .findFirst()
        .map(BearerTokenProvider::getBearerToken)
        .orElse(null);
  }
}
