package com.camunda.consulting;

public record ProcessMetadataResult(Camunda camunda) {
  public record Camunda(
      String processInstanceKey,
      String processDefinitionKey,
      String processDefinitionId,
      int version,
      String versionTag) {}
}
