package com.camunda.consulting;

import com.camunda.consulting.UpdateTaskDto.Data.AssignTaskDto;
import com.camunda.consulting.UpdateTaskDto.Data.CompleteTaskDto;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import java.util.Map;

public record UpdateTaskDto(
    @JsonTypeInfo(use = Id.NAME, property = "changeType", include = As.EXTERNAL_PROPERTY)
        @JsonSubTypes({
          @Type(name = "complete", value = CompleteTaskDto.class),
          @Type(name = "assign", value = AssignTaskDto.class)
        })
        Data data) {

  public sealed interface Data {
    record CompleteTaskDto(Map<String, Object> result) implements Data {}

    record AssignTaskDto(String assignee) implements Data {}
  }
}
