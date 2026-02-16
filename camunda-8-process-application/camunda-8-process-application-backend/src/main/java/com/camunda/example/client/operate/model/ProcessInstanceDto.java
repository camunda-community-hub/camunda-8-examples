package com.camunda.example.client.operate.model;

import lombok.*;

import java.time.*;

@Data
public class ProcessInstanceDto {
  private Long key;
  private Long processVersion;
  private String bpmnProcessId;
  private Long parentKey;
  private ZonedDateTime startDate;
  private ZonedDateTime endDate;
  private String state;
  private String processDefinitionKey;
}
