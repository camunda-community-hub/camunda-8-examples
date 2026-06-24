package com.camunda.example.service.gql;

import com.camunda.example.client.tasklist.model.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface ResponseHandler extends GraphQLOperationHandler {

  void handleResponse(GraphQLOperationDefinition operationDefinition, JsonNode response, ObjectNode requestVariables);
}
