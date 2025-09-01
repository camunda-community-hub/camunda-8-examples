package com.camunda.consulting;

import com.camunda.consulting.UpdateTaskDto.CompleteTaskDto;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.util.Map;

@JsonTypeInfo(use = Id.NAME, property = "changeType")
@JsonSubTypes({@Type(name = "complete", value = CompleteTaskDto.class)})
public sealed interface UpdateTaskDto {
  record CompleteTaskDto(Map<String, Object> result) implements UpdateTaskDto {}
}
