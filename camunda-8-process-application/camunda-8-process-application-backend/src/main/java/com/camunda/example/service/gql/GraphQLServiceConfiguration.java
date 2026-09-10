package com.camunda.example.service.gql;

import com.camunda.example.client.operate.OperateClient;
import com.camunda.example.service.gql.model.GqlBpmnXmlDto;
import com.camunda.example.service.gql.model.GqlPublishMessageResponse;
import com.camunda.example.service.gql.model.VariableInput;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static graphql.schema.idl.RuntimeWiring.*;

@Configuration
public class GraphQLServiceConfiguration {

  @Bean
  public GraphQL graphQL(OperateClient operateClient, ZeebeClient zeebeClient, ObjectMapper objectMapper) throws
      IOException {
    TypeDefinitionRegistry typeDefinitionRegistry = getSchema();
    RuntimeWiring runtimeWiring = newRuntimeWiring()
        .type("Query", typeWiring -> typeWiring.dataFetcher("bpmnXml", environment -> {
          Long processDefinitionId = Long.parseLong(environment.getArgument("processDefinitionId"));
          GqlBpmnXmlDto dto = new GqlBpmnXmlDto();
          dto.setId(processDefinitionId);
          dto.setData(operateClient
              .getProcessDefinitionsEndpoint()
              .xml(processDefinitionId));
          return dto;
        }))
        .type("Mutation", typeWiring -> typeWiring.dataFetcher("publishMessage", environment -> {
          String messageName = environment.getArgument("messageName");
          String correlationKey = environment.getArgument("correlationKey");
          List<VariableInput> variables = objectMapper.convertValue(
              environment.getArgument("variables"),
              new TypeReference<>() {}
          );
          GqlPublishMessageResponse response = new GqlPublishMessageResponse();
          response.setKey(String.valueOf(zeebeClient
              .newPublishMessageCommand()
              .messageName(messageName)
              .correlationKey(correlationKey)
              .variables(variables
                  .stream()
                  .collect(Collectors.toMap(VariableInput::getName, VariableInput::getValue)))
              .send()
              .join()
              .getMessageKey()));
          return response;
        }))
        .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
    return GraphQL
        .newGraphQL(graphQLSchema)
        .build();
  }

  private TypeDefinitionRegistry getSchema() throws IOException {
    try (
        InputStream schema = getClass()
            .getClassLoader()
            .getResourceAsStream("schema.graphqls")
    ) {
      SchemaParser schemaParser = new SchemaParser();
      return schemaParser.parse(schema);
    }
  }
}
