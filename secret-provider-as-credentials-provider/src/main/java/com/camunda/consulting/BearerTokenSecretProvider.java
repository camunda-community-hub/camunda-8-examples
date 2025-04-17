package com.camunda.consulting;

import io.camunda.connector.api.secret.SecretProvider;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A secret provider that returns a token based on the configuration.
 *
 * <p>The format of the secret is {@code bearerToken(serviceName)}.
 *
 * <p>The {@code serviceName} is resolved using the {@link BearerTokenProviderRegistry}.
 */
@Component
public class BearerTokenSecretProvider implements SecretProvider {
  private static final Logger LOG = LoggerFactory.getLogger(BearerTokenSecretProvider.class);
  private static final Pattern SECRET_PATTERN = Pattern.compile("bearerToken\\((.*)\\)");
  private final BearerTokenProviderRegistry registry;

  public BearerTokenSecretProvider(BearerTokenProviderRegistry registry) {
    this.registry = registry;
  }

  @Override
  public String getSecret(String s) {
    Matcher matcher = SECRET_PATTERN.matcher(s);
    if (matcher.find()) {
      String serviceName = matcher.group(1);
      try {
        String bearerTokenForService = registry.getBearerTokenForService(serviceName);
        if (bearerTokenForService == null) {
          LOG.warn("Service '{}' not found, secret '{}' cannot be resolved", serviceName, s);
        }
        return bearerTokenForService;
      } catch (Exception e) {
        LOG.error("Error while resolving bearer token for service '{}'", serviceName, e);
      }
    }
    return null;
  }
}
