package com.camunda.example.client.tasklist.model;

import lombok.*;

import java.util.*;

@Data
public class GraphQLOperationField {
  private String fieldName;
  private Set<GraphQLOperationField> fields;
}
