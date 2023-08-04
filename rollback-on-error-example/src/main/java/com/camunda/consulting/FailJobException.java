package com.camunda.consulting;

import io.camunda.zeebe.gateway.protocol.GatewayOuterClass.FailJobRequest.Builder;

public class FailJobException extends RuntimeException {
  private final Builder builder;

  public FailJobException(Builder builder) {
    super(builder.getErrorMessage());
    this.builder = builder;
  }
}
