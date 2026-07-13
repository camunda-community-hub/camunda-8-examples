package com.camunda.example.client.operate.model;

import lombok.*;

import java.time.*;

@Data
public class IncidentDto {
  private Long key;
  private Long processDefinitionKey;
  private Long processInstanceKey;
  private String type;
  private String message;
  private ZonedDateTime creationTime;
  private String state;
}
