package com.camunda.example.client.operate.model;

import lombok.*;

import java.time.*;

@Data
public class FlowNodeInstanceDto {
  private Long key;
  private Long processInstanceKey;
  private ZonedDateTime startDate;
  private ZonedDateTime endDate;
  private Long incidentKey;
  private String type;
  private String state;
  private Boolean incident;
}
