package com.camunda.example.client.operate.model;

import lombok.*;

@Data
public class ProcessDefinitionDto {
  private Long key;
  private String name;
  private Long version;
  private String bpmnProcessId;
}
