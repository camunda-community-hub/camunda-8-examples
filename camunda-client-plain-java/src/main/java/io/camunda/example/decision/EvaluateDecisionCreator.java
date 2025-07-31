package io.camunda.example.decision;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.EvaluateDecisionResponse;
import io.camunda.example.ClientProvider;
import io.camunda.example.ClientProvider.AuthMethod;

/**
 * Example application that connects to a cluster on Camunda Cloud, or a locally deployed cluster.
 *
 * <p>It uses {@link ClientProvider} to create a client with the appropriate authentication method.
 */
public final class EvaluateDecisionCreator {

  public static void main(final String[] args) {
    final String decisionId = "demoDecision_jedi_or_sith";

    try (final CamundaClient client = ClientProvider.createCamundaClient(AuthMethod.none)) {

      System.out.println("Evaluating decision");

      final EvaluateDecisionResponse decisionEvaluation =
          client
              .newEvaluateDecisionCommand()
              .decisionId(decisionId)
              .variables("{\"lightsaberColor\": \"blue\"}")
              .execute();

      System.out.println("Decision evaluation result: " + decisionEvaluation.getDecisionOutput());
    }
  }
}
