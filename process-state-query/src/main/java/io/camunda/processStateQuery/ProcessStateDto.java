package io.camunda.processStateQuery;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

public record ProcessStateDto(long processInstanceKey, long processDefinitionKey, String bpmnProcessId,
                              String processName, List<ElementInstanceDto> elementInstances,
                              List<IncidentDto> incidents, List<VariableDto> variables, String state,
                              LocalDateTime startDate, LocalDateTime endDate) {
  public record ElementInstanceDto(long elementInstanceKey, String elementId, String elementName,
                                   List<VariableDto> variables, String state, LocalDateTime startDate,
                                   LocalDateTime endDate) {}

  public record IncidentDto(long incidentKey, String message, String state, LocalDateTime creationTime) {}

  public record VariableDto(long variableKey, String name, JsonNode value) {

  }
}
