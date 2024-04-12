package io.camunda.zeebe.example.authorization;

import io.camunda.zeebe.client.CredentialsProvider;
import io.grpc.Status.Code;
import java.io.IOException;

public class MyCredentialsProvider implements CredentialsProvider {

  @Override
  public void applyCredentials(CredentialsApplier applier) throws IOException {
    applier.put("Authorization", "Bearer token");
  }

  @Override
  public boolean shouldRetryRequest(StatusCode statusCode) {
    return statusCode.code() == Code.DEADLINE_EXCEEDED.value();
  }
}
