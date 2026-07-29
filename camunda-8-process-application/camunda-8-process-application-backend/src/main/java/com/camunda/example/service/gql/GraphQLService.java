package com.camunda.example.service.gql;

import com.camunda.example.client.tasklist.TasklistClient;
import com.camunda.example.client.tasklist.model.GraphQLOperationDefinition;
import com.camunda.example.client.tasklist.model.GraphQLOperationField;
import com.camunda.example.client.tasklist.model.GraphQLRequestDto;
import com.camunda.example.client.tasklist.model.GraphQLResponseDto;
import com.camunda.example.client.tasklist.model.GraphQLResponseDto.ErrorDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import graphql.language.AstPrinter;
import graphql.language.Document;
import graphql.language.Field;
import graphql.language.FragmentDefinition;
import graphql.language.NodeChildrenContainer;
import graphql.language.OperationDefinition;
import graphql.language.SelectionSet;
import graphql.language.VariableReference;
import graphql.parser.Parser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Optional.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphQLService {

  private final TasklistClient tasklistClient;
  private final Set<ResponseHandler> responseHandlers;
  private final Set<RequestVariableHandler> requestVariableHandlers;

  private final CustomGraphQLService customGraphQLService;

  public GraphQLResponseDto<ObjectNode> executeQuery(GraphQLRequestDto requestDto) {
    Document query = Parser.parse(requestDto.getQuery());
    return extractOperationWithVariableNames(query)
        .peek(operationDefinition -> adjustVariables(operationDefinition, requestDto.getVariables()))
        .flatMap(operationDefinition -> doExecute(operationDefinition,
            requestDto.getVariables()
        ).map(responseDto -> Map.entry(
            operationDefinition,
            responseDto
        )))
        .peek(e -> adjustResult(e.getKey(),
            e
                .getValue()
                .getData(),
            requestDto.getVariables()
        ))
        .map(Entry::getValue)
        .reduce(new GraphQLResponseDto<>(), (dto1, dto2) -> merge(dto1, dto2, true));
  }

  private GraphQLResponseDto<ObjectNode> merge(
      GraphQLResponseDto<ObjectNode> response1, GraphQLResponseDto<ObjectNode> response2, boolean throwOnConflict
  ) {
    GraphQLResponseDto<ObjectNode> response = new GraphQLResponseDto<>();
    // merge execution results
    ObjectNode data = response1.getData();
    ObjectNode data2 = response2.getData();
    if (data != null && data2 != null) {
      response.setData((ObjectNode) merge(data, data2, throwOnConflict));
    } else if (data2 != null) {
      response.setData(data2);
    } else {
      response.setData(data);
    }
    List<ErrorDto> errors = response1.getErrors();
    List<ErrorDto> errors2 = response2.getErrors();
    if (errors != null && errors2 != null) {
      errors.addAll(errors2);
      response.setErrors(errors);
    } else if (errors2 != null) {
      response.setErrors(errors2);
    }
    return response;
  }

  private JsonNode merge(JsonNode node1, JsonNode node2, boolean throwOnConflict) {
    if (node1 == null || node1.isNull()) {
      return node2;
    }
    if (node2 == null || node2.isNull()) {
      return node1;
    }
    if (node1
        .getNodeType()
        .equals(node2.getNodeType())) {
      if (node1.isArray()) {
        ArrayNode array1 = (ArrayNode) node1;
        ArrayNode array2 = (ArrayNode) node2;
        return JsonNodeFactory.instance
            .arrayNode()
            .addAll(Stream
                .concat(StreamSupport.stream(array1.spliterator(), false),
                    StreamSupport.stream(array2.spliterator(), false)
                )
                .collect(Collectors.toSet()));
      } else if (node1.isObject()) {
        node2
            .fields()
            .forEachRemaining(e -> {
              if (node1.get(e.getKey()) != null) {
                ((ObjectNode) node1).replace(e.getKey(),
                    merge(node1.get(e.getKey()), node2.get(e.getKey()), throwOnConflict)
                );
              } else {
                ((ObjectNode) node1).set(e.getKey(), e.getValue());
              }
            });
        return node1;
      } else {
        if (node1.equals(node2) || !throwOnConflict) {
          return node1;
        } else {
          throw new RuntimeException("Nodes cannot be merged, atomic value is not equal: " + node1 + ", " + node2);
        }
      }
    }
    throw new RuntimeException("Nodes cannot be merged, types are not equal: " + node1 + ", " + node2);
  }

  private Stream<GraphQLResponseDto<ObjectNode>> doExecute(
      GraphQLOperationDefinition operationDefinition, ObjectNode variables
  ) {

    NodeChildrenContainer nodeChildrenContainer = NodeChildrenContainer
        .newNodeChildrenContainer()
        .child(OperationDefinition.CHILD_SELECTION_SET,
            SelectionSet
                .newSelectionSet()
                .selection(operationDefinition.getOperation())
                .build()
        )
        .children(OperationDefinition.CHILD_VARIABLE_DEFINITIONS,
            operationDefinition
                .getOperationDefinition()
                .getVariableDefinitions()
        )
        .build();
    OperationDefinition op = operationDefinition
        .getOperationDefinition()
        .withNewChildren(nodeChildrenContainer);
    Document document = Document
        .newDocument()
        .definitions(new ArrayList<>(operationDefinition.getFragmentDefinitions()))
        .definition(op)
        .build();
    GraphQLRequestDto dto = new GraphQLRequestDto();
    dto.setQuery(AstPrinter.printAst(document));
    dto.setVariables(variables);
    if (operationDefinition
        .getOperationName()
        .equals("__schema")) {
      return Stream.of(mergeSchemas(customGraphQLService.execute(dto), tasklistClient.executeQuery(dto)));
    } else {
      if (customGraphQLService.canExecute(operationDefinition)) {
        return Stream.of(customGraphQLService.execute(dto));
      } else {
        return Stream.of(tasklistClient.executeQuery(dto));
      }
    }
  }

  private GraphQLResponseDto<ObjectNode> mergeSchemas(
      GraphQLResponseDto<ObjectNode> schema1, GraphQLResponseDto<ObjectNode> schema2
  ) {

    GraphQLResponseDto<ObjectNode> result = merge(schema1, schema2, false);

    // ensure there is only 1 query field -> merge all existing
    // ensure there is only 1 mutation field -> merge all existing
    // ensure there is only 1 subscription field -> merge all existing
    reduceTypes(result).ifPresent(types -> {
      ObjectNode schema = (ObjectNode) result
          .getData()
          .get("__schema");
      ArrayNode typesArray = JsonNodeFactory.instance.arrayNode();
      types.forEach(typesArray::add);
      schema.set("types", typesArray);
    });
    reduceDirectives(result).ifPresent(directives -> {
      ObjectNode schema = (ObjectNode) result
          .getData()
          .get("__schema");
      ArrayNode directivesArray = JsonNodeFactory.instance.arrayNode();
      directives.forEach(directivesArray::add);
      schema.set("directives", directivesArray);
    });
    return result;
  }

  private Optional<Set<JsonNode>> reduceDirectives(GraphQLResponseDto<ObjectNode> result) {
    return ofNullable(result.getData())
        .map(get("__schema"))
        .map(get("directives"))
        .map(ArrayNode.class::cast)
        .map(directives -> StreamSupport
            .stream(directives.spliterator(), false)
            .collect(Collectors.groupingBy(directive -> directive
                .get("name")
                .asText()))
            .values()
            .stream()
            .map(list -> list
                .stream()
                .reduce(JsonNodeFactory.instance.objectNode(), (json1, json2) -> merge(json1, json2, false)))
            .collect(Collectors.toSet()));
  }

  private Optional<Set<JsonNode>> reduceTypes(GraphQLResponseDto<ObjectNode> result) {
    return ofNullable(result.getData())
        .map(get("__schema"))
        .map(get("types"))
        .map(ArrayNode.class::cast)
        .map(types -> StreamSupport
            .stream(types.spliterator(), false)
            .collect(Collectors.groupingBy(type -> type
                    .get("kind")
                    .asText(),
                Collectors.groupingBy(type -> type
                    .get("name")
                    .asText())
            ))
            .values()
            .stream()
            .flatMap(e -> e
                .values()
                .stream())
            .map(list -> list
                .stream()
                .reduce(JsonNodeFactory.instance.objectNode(), (json1, json2) -> merge(json1, json2, false)))
            .collect(Collectors.toSet()));
  }

  private Function<JsonNode, JsonNode> get(String fieldName) {
    return jsonNode -> jsonNode.get(fieldName);
  }

  private Stream<GraphQLOperationDefinition> extractOperationWithVariableNames(Document document) {
    return document
        .getDefinitionsOfType(OperationDefinition.class)
        .stream()
        .flatMap(operationDefinition -> operationDefinition
            .getSelectionSet()
            .getSelectionsOfType(Field.class)
            .stream()
            .map(field -> {
              GraphQLOperationDefinition definition = new GraphQLOperationDefinition();
              definition.setFragmentDefinitions(new HashSet<>(document.getDefinitionsOfType(FragmentDefinition.class)));
              definition.setOperationDefinitionType(operationDefinition.getOperation());
              definition.setOperationDefinitionName(operationDefinition.getName());
              definition.setOperationDefinition(operationDefinition);
              definition.setOperation(field);
              definition.setOperationName(field.getName());
              definition.setVariableMappings(field
                  .getArguments()
                  .stream()
                  .map(argument -> Map.entry(argument.getName(), argument.getValue()))
                  .filter(entry -> VariableReference.class.isAssignableFrom(entry
                      .getValue()
                      .getClass()))
                  .map(entry -> Map.entry(entry.getKey(), (VariableReference) entry.getValue()))
                  .map(entry -> Map.entry(entry.getKey(),
                      entry
                          .getValue()
                          .getName()
                  ))
                  .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
              definition.setFields(extractFields(field
                  .getSelectionSet()
                  .getSelectionsOfType(Field.class)));
              return definition;
            }));
  }

  private Set<GraphQLOperationField> extractFields(List<Field> fields) {
    return fields
        .stream()
        .map(field -> {
          GraphQLOperationField f = new GraphQLOperationField();
          f.setFieldName(field.getName());
          if (field.getSelectionSet() != null) {
            f.setFields(extractFields(field
                .getSelectionSet()
                .getSelectionsOfType(Field.class)));
          } else {
            f.setFields(Set.of());
          }
          return f;
        })
        .collect(Collectors.toSet());
  }

  private void adjustVariables(GraphQLOperationDefinition operation, ObjectNode requestVariables) {
    if (requestVariables == null) {
      return;
    }
    requestVariableHandlers
        .stream()
        .filter(requestVariableHandler -> requestVariableHandler.canHandle(operation))
        .forEach(requestVariableHandler -> requestVariableHandler.handleRequestVariables(operation, requestVariables));

  }

  private void adjustResult(
      GraphQLOperationDefinition operation, ObjectNode responseData, ObjectNode requestVariables
  ) {
    if (responseData == null) {
      return;
    }
    prepareResponseData(responseData, operation.getOperationName());
    responseHandlers
        .stream()
        .filter(operationHandler -> null != responseData.get(operation.getOperationName()))
        .filter(operationHandler -> operationHandler.canHandle(operation))
        .forEach(operationHandler -> operationHandler.handleResponse(operation,
            responseData.get(operation.getOperationName()),
            requestVariables
        ));
  }

  private void prepareResponseData(ObjectNode responseData, String operationName) {
    if (responseData
        .get(operationName)
        .isNull()) {
      responseData.replace(operationName, JsonNodeFactory.instance.objectNode());
    }
  }

}
