package com.camunda.example;

import com.camunda.example.client.operate.*;
import graphql.*;
import graphql.schema.*;
import graphql.schema.idl.*;
import lombok.*;
import lombok.Builder;
import org.junit.jupiter.api.*;

import java.io.*;

import static graphql.schema.idl.RuntimeWiring.*;
import static org.mockito.Mockito.*;

public class GraphQLTest {

  @Test
  public void should() throws IOException {
    OperateClient client = mock(OperateClient.class);
    when(client.getProcessDefinitionsEndpoint()).thenReturn(mock(OperateProcessDefinitionsEndpoint.class));
    when(client.getProcessDefinitionsEndpoint().xml(anyLong())).thenReturn("abc");

    TypeDefinitionRegistry typeDefinitionRegistry = getSchema();
    RuntimeWiring runtimeWiring = newRuntimeWiring()
        .type("QueryType", typeWiring -> typeWiring.dataFetcher("bpmnXml", environment -> {
          Long processDefinitionId = Long.parseLong(environment.getArgument("processDefinitionId"));
          String data = client
              .getProcessDefinitionsEndpoint()
              .xml(processDefinitionId);
          return BpmnXmlDto
              .builder()
              .id(processDefinitionId)
              .data(data)
              .build();
        }))
//        .type(
//            "BpmnXml",
//            typeWiring -> typeWiring
//                .dataFetcher("id", environment -> ((BpmnXmlDto) environment.getSource()).getId())
//                .dataFetcher("data", environment -> ((BpmnXmlDto) environment.getSource()).getData())
//        )
        .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    GraphQL build = GraphQL
        .newGraphQL(graphQLSchema)
        .build();
    ExecutionResult executionResult = build.execute("query { bpmnXml(processDefinitionId: 123){id  data}}");
    System.out.println(executionResult
        .getData()
        .toString());
    // Prints: {hello=world}
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

  @Data
  @Builder
  private static class BpmnXmlDto {
    private Long id;
    private String data;
  }
}
