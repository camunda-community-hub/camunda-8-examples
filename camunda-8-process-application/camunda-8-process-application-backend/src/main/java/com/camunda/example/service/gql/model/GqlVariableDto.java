package com.camunda.example.service.gql.model;

import lombok.*;

@Data
public class GqlVariableDto {
  private String id;
  private String name;
  private String value;
  private String previewValue;
  private Boolean isValueTruncated;
}
