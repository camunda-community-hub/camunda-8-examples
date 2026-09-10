package com.camunda.example.client.operate.model;

import lombok.*;

@Data
public class VariableDto {
  private Long key;
  private Long processInstanceKey;
  private Long scopeKey;
  private String name;
  private String value;
  private Boolean truncated;
}
