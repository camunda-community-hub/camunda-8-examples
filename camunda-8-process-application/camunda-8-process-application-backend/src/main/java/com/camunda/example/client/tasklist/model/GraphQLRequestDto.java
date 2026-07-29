package com.camunda.example.client.tasklist.model;

import com.fasterxml.jackson.databind.node.*;
import lombok.Data;

import java.util.Map;

@Data
public class GraphQLRequestDto {
  private String query;
  private ObjectNode variables;
}
