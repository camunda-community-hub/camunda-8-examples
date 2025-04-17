package com.camunda.consulting;

import io.camunda.zeebe.client.CredentialsProvider;
import io.camunda.zeebe.client.CredentialsProvider.CredentialsApplier;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * This is an example implementation of {@link BearerTokenProvider}.
 *
 * <p>It uses the {@link CredentialsProvider} bean of the zeebe spring sdk to retrieve a token for
 * the zeebe api.
 */
@Component
public class ZeebeBearerTokenProvider implements BearerTokenProvider {
  private final CredentialsProvider credentialsProvider;

  public ZeebeBearerTokenProvider(CredentialsProvider credentialsProvider) {
    this.credentialsProvider = credentialsProvider;
  }

  @Override
  public String getServiceName() {
    return "zeebe";
  }

  @Override
  public String getBearerToken() {
    MapCredentialsApplier credentialsApplier = new MapCredentialsApplier();
    try {
      credentialsProvider.applyCredentials(credentialsApplier);
    } catch (IOException e) {
      throw new RuntimeException("Error while applying zeebe credentials", e);
    }
    String authorization = credentialsApplier.getCredentials().get("Authorization");
    return authorization.substring("Bearer ".length());
  }

  private static class MapCredentialsApplier implements CredentialsApplier {
    private final Map<String, String> credentials = new HashMap<>();

    @Override
    public void put(String key, String value) {
      credentials.put(key, value);
    }

    public Map<String, String> getCredentials() {
      return credentials;
    }
  }
}
