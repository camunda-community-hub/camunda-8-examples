package com.camunda.example.client.tasklist.model;

import graphql.language.*;
import graphql.language.OperationDefinition.*;
import lombok.*;

import java.util.*;

@Data
public class GraphQLOperationDefinition {
  private Operation operationDefinitionType;
  private String operationDefinitionName;
  private OperationDefinition operationDefinition;
  private Field operation;
  private String operationName;
  private Map<String, String> variableMappings;
  private Set<GraphQLOperationField> fields;
  private Set<FragmentDefinition> fragmentDefinitions;
}
