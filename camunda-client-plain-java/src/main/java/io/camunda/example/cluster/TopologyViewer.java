package io.camunda.example.cluster;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.Topology;
import io.camunda.example.ClientProvider;
import io.camunda.example.ClientProvider.AuthMethod;

/**
 * Example application that connects to a cluster on Camunda Cloud, or a locally deployed cluster.
 *
 * <p>It uses {@link ClientProvider} to create a client with the appropriate authentication method.
 */
public final class TopologyViewer {

  public static void main(final String[] args) {
    try (final CamundaClient client = ClientProvider.createCamundaClient(AuthMethod.none)) {
      final Topology topology = client.newTopologyRequest().execute();

      System.out.println("Topology:");
      topology
          .getBrokers()
          .forEach(
              b -> {
                System.out.println("    " + b.getAddress());
                b.getPartitions()
                    .forEach(
                        p ->
                            System.out.println(
                                "      " + p.getPartitionId() + " - " + p.getRole()));
              });

      System.out.println("Done.");
    }
  }
}
