package com.camunda.example.client.tasklist.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GraphQLResponseDto<T> {
  private T data;
  private List<ErrorDto> errors;

  @Data
  public static class ErrorDto {
    private String message;
    private List<ErrorLocation> locations;
    private Map<String, Object> extensions;
  }

  @Data
  public static class ErrorLocation {
    private Integer line;
    private Integer column;
  }
}
