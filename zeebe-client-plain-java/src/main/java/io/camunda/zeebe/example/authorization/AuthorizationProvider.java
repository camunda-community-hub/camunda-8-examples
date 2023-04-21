package io.camunda.zeebe.example.authorization;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.impl.ZeebeClientBuilderImpl;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;


/**
 * Example application that connects to a locally deployed cluster.
 * When connecting to a cluster, this application uses the following default information to connect to a cluster with Camunda's Identity as an identity provider option:
 * <ul>
 *   <li>ZEEBE_ADDRESS
 *   <li>ZEEBE_CLIENT_ID
 *   <li>ZEEBE_CLIENT_SECRET
 *   <li>ZEEBE_AUTHORIZATION_SERVER_URL
 * </ul>
 * */

public class AuthorizationProvider {
    public static void main(final String[] args) {

    final String ZEEBE_ADDRESS = "localhost:26500";
    final String ZEEBE_CLIENT_ID = "zeebe";
    final String ZEEBE_CLIENT_SECRET = "zecret";
    final String ZEEBE_CLIENT_AUDIENCE = "zeebe-api";
    final String ZEEBE_AUTHORIZATION_SERVER_URL = "http://localhost:18080/auth/realms/camunda-platform/protocol/openid-connect/token";




    /*

    // Connect to local deployment. Assumes that authentication is disabled.
    final ZeebeClientBuilder zeebeClientBuilder = ZeebeClient.newClientBuilder().gatewayAddress(ZEEBE_ADDRESS).usePlaintext();
    final ZeebeClient client = zeebeClientBuilder.build();

     */



    /*

    //Connect to local deployment with Bearer token in header. Assumes that authentication is enabled.
    final ZeebeClientBuilder zeebeClientBuilder = ZeebeClient.newClientBuilder().credentialsProvider(new MyCredentialsProvider()).gatewayAddress(ZEEBE_ADDRESS).usePlaintext();
    final ZeebeClient client = zeebeClientBuilder.build();

   */

        //Connect to a local deployment with OAuthCredentialsProvider with Identity. Assumes authentication is enabled.

    final OAuthCredentialsProvider provider =
            new OAuthCredentialsProviderBuilder()
                    .clientId(ZEEBE_CLIENT_ID)
                    .clientSecret(ZEEBE_CLIENT_SECRET)
                    .audience(ZEEBE_CLIENT_AUDIENCE)
                    .authorizationServerUrl(ZEEBE_AUTHORIZATION_SERVER_URL)
                    .build();

    final ZeebeClient client =
            new ZeebeClientBuilderImpl()
                    .gatewayAddress(ZEEBE_ADDRESS).usePlaintext()
                    .credentialsProvider(provider)
                    .build();



        System.out.println("Zeebe topology: " + client.newTopologyRequest().send().join().toString());

}
}
