package com.camunda.example.service.gql;

import com.camunda.example.client.tasklist.model.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import graphql.language.OperationDefinition.*;
import org.springframework.stereotype.*;

import java.util.*;

@Component
public class BusinessDataFilteringHandler implements RequestVariableHandler {
  private final Set<String> VARIABLES_TO_FILTER = Set.of("applicantName");

  @Override
  public boolean canHandle(GraphQLOperationDefinition operationDefinition) {
    return operationDefinition
        .getOperation()
        .equals(Operation.MUTATION) && operationDefinition
        .getOperationName()
        .equals("completeTask") && operationDefinition
        .getVariableMappings()
        .containsKey("variables");
  }

  @Override
  public void handleRequestVariables(GraphQLOperationDefinition operationDefinition, ObjectNode requestVariables) {
    ArrayNode variables = (ArrayNode) requestVariables.get(operationDefinition
        .getVariableMappings()
        .get("variables"));
    ArrayNode newVariables = variables.arrayNode();
    for (JsonNode variable : variables) {
      if (!VARIABLES_TO_FILTER.contains(variable
          .get("name")
          .asText())) {
        newVariables.add(variable);
      }
    }
    variables.removeAll();
    variables.addAll(newVariables);
  }
}
