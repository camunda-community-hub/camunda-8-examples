package com.camunda.example.service.gql;

import com.camunda.example.client.tasklist.model.*;
import com.fasterxml.jackson.databind.node.*;
import graphql.language.OperationDefinition.*;
import org.springframework.security.core.context.*;
import org.springframework.stereotype.*;

@Component
public class TaskQueryHandler implements RequestVariableHandler {
  @Override
  public boolean canHandle(GraphQLOperationDefinition operationDefinition) {
    return operationDefinition
        .getOperation()
        .equals(Operation.QUERY) && operationDefinition
        .getOperationName()
        .equals("tasks");
  }

  @Override
  public void handleRequestVariables(GraphQLOperationDefinition operationDefinition, ObjectNode requestVariables) {
    String queryVariableName = operationDefinition
        .getVariableMappings()
        .get("query");
    ObjectNode query = (ObjectNode) requestVariables.get(queryVariableName);
    String role = SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getAuthorities()
        .stream()
        .findFirst()
        .get()
        .toString()
        .split("_")[1];
    query.put("candidateGroup", role);
  }
}
