package com.camunda.example.service.gql;

import com.camunda.example.client.tasklist.model.GraphQLOperationDefinition;
import com.camunda.example.service.business.CamundaFormService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import graphql.language.Argument;
import graphql.language.Field;
import graphql.language.OperationDefinition.Operation;
import graphql.language.VariableReference;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class CamundaFormsHandler implements ResponseHandler {
  private final CamundaFormService camundaFormService;

  @Override
  public boolean canHandle(GraphQLOperationDefinition operationDefinition) {
    return operationDefinition
        .getOperationDefinitionType()
        .equals(Operation.QUERY) && operationDefinition
        .getOperationName()
        .equals("form");
  }

  @Override
  public void handleResponse(
      GraphQLOperationDefinition operationDefinition, JsonNode response, ObjectNode requestVariables
  ) {
    if (!response.isEmpty()) {
      return;
    }
    findVariable("id", operationDefinition, requestVariables).ifPresent(id -> findVariable("processDefinitionId",
        operationDefinition,
        requestVariables
    ).ifPresent(processDefinitionId -> Optional
        .ofNullable(camundaFormService.getSchema(id))
        .ifPresent(schema -> {
          ObjectNode responseObject = (ObjectNode) response;
          if (fieldQueried("schema", operationDefinition)) {
            responseObject.put("schema", schema);
          }
          if (fieldQueried("id", operationDefinition)) {
            responseObject.put("id", id);
          }
          if (fieldQueried("processDefinitionId", operationDefinition)) {
            responseObject.put("processDefinitionId", processDefinitionId);
          }
        })));
  }

  private Optional<String> findVariable(
      String argName, GraphQLOperationDefinition operationDefinition, ObjectNode requestVariables
  ) {
    return operationDefinition
        .getOperation()
        .getArguments()
        .stream()
        .filter(arg -> arg
            .getName()
            .equals(argName))
        .map(Argument::getValue)
        .filter(value -> VariableReference.class.isAssignableFrom(value.getClass()))
        .map(VariableReference.class::cast)
        .map(variableReference -> requestVariables.get(variableReference.getName()))
        .filter(JsonNode::isTextual)
        .map(JsonNode::textValue)
        .findFirst();
  }

  private boolean fieldQueried(String fieldName, GraphQLOperationDefinition operationDefinition) {
    return operationDefinition
        .getOperationDefinition()
        .getSelectionSet()
        .getSelectionsOfType(Field.class)
        .stream()
        .filter(field -> field
            .getName()
            .equals("form"))
        .findFirst()
        .flatMap(field -> field
            .getSelectionSet()
            .getSelectionsOfType(Field.class)
            .stream()
            .filter(innerField -> innerField
                .getName()
                .equals(fieldName))
            .findFirst())
        .isPresent();
  }
}
