package com.camunda.example.service.gql;

import com.camunda.example.client.tasklist.model.*;
import com.fasterxml.jackson.databind.node.*;

public interface RequestVariableHandler extends GraphQLOperationHandler {

  void handleRequestVariables(GraphQLOperationDefinition operationDefinition, ObjectNode requestVariables);
}
