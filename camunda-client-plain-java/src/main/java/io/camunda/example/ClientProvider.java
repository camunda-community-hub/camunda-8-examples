package io.camunda.example;

import static java.util.Optional.*;

import io.camunda.client.CamundaClient;
import io.camunda.client.CredentialsProvider;
import io.camunda.client.impl.NoopCredentialsProvider;
import io.camunda.client.impl.basicauth.BasicAuthCredentialsProviderBuilder;
import java.net.URI;
import java.util.function.Function;

public class ClientProvider {
  /**
   * Creates a CamundaClient with the given authentication method.
   *
   * <p>When connecting to a Camunda instance, this application assumes that the following
   * environment variables are set:
   *
   * <ul>
   *   <li>{@code CAMUNDA_CLIENT_GRPCADDRESS}
   *   <li>{@code CAMUNDA_CLIENT_RESTADDRESS}
   * </ul>
   *
   * <p>In addition, if {@link AuthMethod}{@code .oidc} is selected, the following environment
   * variables are also looked up:
   *
   * <ul>
   *   <li>{@code CAMUNDA_CLIENT_AUTH_CLIENTID}
   *   <li>{@code CAMUNDA_CLIENT_AUTH_CLIENTSECRET}
   *   <li>{@code CAMUNDA_CLIENT_AUTH_AUDIENCE}
   *   <li>{@code CAMUNDA_CLIENT_AUTH_TOKENURL}
   * </ul>
   *
   * <p>If {@link AuthMethod}{@code .basic} is selected, the following environment variables are
   * also looked up:
   *
   * <ul>
   *   <li>{@code CAMUNDA_CLIENT_USERNAME}
   *   <li>{@code CAMUNDA_CLIENT_PASSWORD}
   * </ul>
   *
   * <p>If {@link AuthMethod}{@code .saas} is selected, the following environment variables are also
   * looked up:
   *
   * <ul>
   *   <li>{@code CAMUNDA_CLUSTER_ID}
   *   <li>{@code CAMUNDA_CLIENT_ID}
   *   <li>{@code CAMUNDA_CLIENT_SECRET}
   *   <li>{@code CAMUNDA_CLUSTER_REGION}
   * </ul>
   *
   * <p><strong>Hint:</strong> When you create client credentials in Camunda Cloud you have the
   * option to download a file with above lines filled out for you.
   *
   * <p>When {@code CAMUNDA_CLIENT_GRPCADDRESS} and {@code CAMUNDA_CLIENT_RESTADDRESS} are not set,
   * it connects to a broker running on localhost with default ports
   */
  public static CamundaClient createCamundaClient(AuthMethod authMethod) {
    final URI CAMUNDA_CLIENT_GRPCADDRESS =
        envOrDefault(
            "CAMUNDA_CLIENT_GRPCADDRESS", URI.create("http://localhost:26500"), URI::create);
    final URI CAMUNDA_CLIENT_RESTADDRESS =
        envOrDefault(
            "CAMUNDA_CLIENT_RESTADDRESS", URI.create("http://localhost:8088"), URI::create);
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
    final String CAMUNDA_CLIENT_USERNAME = envOrDefault("CAMUNDA_CLIENT_USERNAME", "demo");
    final String CAMUNDA_CLIENT_PASSWORD = envOrDefault("CAMUNDA_CLIENT_PASSWORD", "demo");
    final String CAMUNDA_CLUSTER_ID =
        envOrDefault("CAMUNDA_CLUSTER_ID", "[Cluster ID from Console]");
    final String CAMUNDA_CLIENT_ID = envOrDefault("CAMUNDA_CLIENT_ID", "[Client ID from Console]");
    final String CAMUNDA_CLIENT_SECRET =
        envOrDefault("CAMUNDA_CLIENT_SECRET", "[Client Secret from Console]");
    final String CAMUNDA_CLUSTER_REGION =
        envOrDefault("CAMUNDA_CLUSTER_REGION", "[Cluster Region from Console]");

    CredentialsProvider provider =
        switch (authMethod) {
          case oidc ->
              CredentialsProvider.newCredentialsProviderBuilder()
                  .clientId(CAMUNDA_CLIENT_AUTH_CLIENTID)
                  .clientSecret(CAMUNDA_CLIENT_AUTH_CLIENTSECRET)
                  .audience(CAMUNDA_CLIENT_AUTH_AUDIENCE)
                  .authorizationServerUrl(CAMUNDA_CLIENT_AUTH_TOKENURL)
                  .build();
          case basic ->
              new BasicAuthCredentialsProviderBuilder()
                  .username(CAMUNDA_CLIENT_USERNAME)
                  .password(CAMUNDA_CLIENT_PASSWORD)
                  .build();
          case none, saas -> new NoopCredentialsProvider();
        };

    final CamundaClient client;
    if (authMethod == AuthMethod.saas) {
      client =
          CamundaClient.newCloudClientBuilder()
              .withClusterId(CAMUNDA_CLUSTER_ID)
              .withClientId(CAMUNDA_CLIENT_ID)
              .withClientSecret(CAMUNDA_CLIENT_SECRET)
              .withRegion(CAMUNDA_CLUSTER_REGION)
              .build();
    } else if (authMethod == AuthMethod.none) {
      client =
          CamundaClient.newClientBuilder()
              .grpcAddress(CAMUNDA_CLIENT_GRPCADDRESS)
              .usePlaintext()
              .restAddress(CAMUNDA_CLIENT_RESTADDRESS)
              .credentialsProvider(provider)
              .build();
    } else {
      client =
          CamundaClient.newClientBuilder()
              .grpcAddress(CAMUNDA_CLIENT_GRPCADDRESS)
              .restAddress(CAMUNDA_CLIENT_RESTADDRESS)
              .credentialsProvider(provider)
              .build();
    }

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
    oidc,
    basic,
    saas
  }
}
