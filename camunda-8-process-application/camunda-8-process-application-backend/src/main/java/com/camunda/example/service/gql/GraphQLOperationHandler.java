package com.camunda.example.service.gql;

import com.camunda.example.client.tasklist.model.*;

public interface GraphQLOperationHandler {
  boolean canHandle(GraphQLOperationDefinition operationDefinition);
}
