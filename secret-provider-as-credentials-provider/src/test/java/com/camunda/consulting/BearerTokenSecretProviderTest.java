package com.camunda.consulting;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import org.junit.jupiter.api.Test;

public class BearerTokenSecretProviderTest {
  @Test
  void shouldFindSecret() {
    BearerTokenSecretProvider provider =
        new BearerTokenSecretProvider(
            new BearerTokenProviderRegistry(Collections.singleton(new Peter())));
    String secret = provider.getSecret("bearerToken(peter)");
    assertEquals("hase", secret);
  }

  @Test
  void shouldNotFindSecret() {
    BearerTokenSecretProvider provider =
        new BearerTokenSecretProvider(
            new BearerTokenProviderRegistry(Collections.singleton(new Peter())));
    String secret = provider.getSecret("bearerToken(hase)");
    assertNull(secret);
  }

  static class Peter implements BearerTokenProvider {
    @Override
    public String getBearerToken() {
      return "hase";
    }

    @Override
    public String getServiceName() {
      return "peter";
    }
  }
}
