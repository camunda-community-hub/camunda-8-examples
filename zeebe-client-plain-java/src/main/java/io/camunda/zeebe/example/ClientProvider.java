package io.camunda.zeebe.example;

import static java.util.Optional.*;

import io.camunda.zeebe.client.CredentialsProvider;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.impl.NoopCredentialsProvider;
import java.net.URI;
import java.util.function.Function;

public class ClientProvider {
  /**
   * Creates a ZeebeClient with the given authentication method.
   *
   * <p>When connecting to a Camunda instance, this application assumes that the following
   * environment variables are set:
   *
   * <ul>
   *   <li>{@code CAMUNDA_CLIENT_GRPCADDRESS}
   *   <li>{@code CAMUNDA_CLIENT_RESTADDRESS}
   * </ul>
   *
   * <p>In addition, if {@link AuthMethod}{@code .none} is selected, the following environment
   * variables are also looked up:
   *
   * <ul>
   *   <li>{@code CAMUNDA_CLIENT_AUTH_CLIENTID}
   *   <li>{@code CAMUNDA_CLIENT_AUTH_CLIENTSECRET}
   *   <li>{@code CAMUNDA_CLIENT_AUTH_AUDIENCE}
   *   <li>{@code CAMUNDA_CLIENT_AUTH_TOKENURL}
   * </ul>
   *
   * <p><strong>Hint:</strong> When you create client credentials in Camunda Cloud you have the
   * option to download a file with above lines filled out for you.
   *
   * <p>When {@code CAMUNDA_CLIENT_GRPCADDRESS} and {@code CAMUNDA_CLIENT_RESTADDRESS} are not set,
   * it connects to a broker running on localhost with default ports
   */
  public static ZeebeClient createZeebeClient(AuthMethod authMethod) {
    final URI CAMUNDA_CLIENT_ZEEBE_GRPCADDRESS =
        envOrDefault(
            "CAMUNDA_CLIENT_ZEEBE_GRPCADDRESS", URI.create("http://localhost:26500"), URI::create);
    final URI CAMUNDA_CLIENT_ZEEBE_RESTADDRESS =
        envOrDefault(
            "CAMUNDA_CLIENT_ZEEBE_RESTADDRESS", URI.create("http://localhost:8088"), URI::create);
    final String CAMUNDA_CLIENT_AUTH_CLIENTID =
        envOrDefault("CAMUNDA_CLIENT_AUTH_CLIENTID", "zeebe");
    final String CAMUNDA_CLIENT_AUTH_CLIENTSECRET =
        envOrDefault("CAMUNDA_CLIENT_AUTH_CLIENTSECRET", "zecret");
    final String CAMUNDA_CLIENT_AUTH_AUDIENCE =
        envOrDefault("CAMUNDA_CLIENT_AUTH_AUDIENCE", "zeebe-api");
    final String CAMUNDA_CLIENT_AUTH_TOKENURL =
        envOrDefault(
            "CAMUNDA_CLIENT_AUTH_TOKENURL",
            "http://localhost:18080/auth/realms/camunda-platform/protocol/openid-connect/token");
    CredentialsProvider provider =
        switch (authMethod) {
          case oidc ->
              CredentialsProvider.newCredentialsProviderBuilder()
                  .clientId(CAMUNDA_CLIENT_AUTH_CLIENTID)
                  .clientSecret(CAMUNDA_CLIENT_AUTH_CLIENTSECRET)
                  .audience(CAMUNDA_CLIENT_AUTH_AUDIENCE)
                  .authorizationServerUrl(CAMUNDA_CLIENT_AUTH_TOKENURL)
                  .build();
          case none -> new NoopCredentialsProvider();
        };

    final ZeebeClient client =
        ZeebeClient.newClientBuilder()
            .grpcAddress(CAMUNDA_CLIENT_ZEEBE_GRPCADDRESS)
            .restAddress(CAMUNDA_CLIENT_ZEEBE_RESTADDRESS)
            .credentialsProvider(provider)
            .build();

    return client;
  }

  private static String envOrDefault(String envVar, String defaultValue) {
    return ofNullable(System.getenv(envVar)).orElse(defaultValue);
  }

  private static <T> T envOrDefault(String envVar, T defaultValue, Function<String, T> converter) {
    return ofNullable(System.getenv(envVar)).map(converter).orElse(defaultValue);
  }

  public enum AuthMethod {
    none,
    oidc
  }
}
