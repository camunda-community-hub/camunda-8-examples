package com.camunda.consulting;

/**
 * This is the interface that describes the contract between the {@link BearerTokenProviderRegistry}
 * and each individual implementation.
 */
public interface BearerTokenProvider {
  String getServiceName();

  String getBearerToken();
}
