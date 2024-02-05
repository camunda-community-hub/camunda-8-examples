package com.camunda.example.client.operate;

import com.camunda.example.client.operate.model.*;
import com.fasterxml.jackson.databind.*;
import io.camunda.zeebe.client.*;
import org.springframework.core.*;
import org.springframework.web.client.*;

import java.net.*;

public class OperateVariablesEndpoint extends AbstractOperateObjectEndpoint<VariableDto> {
  private static final ParameterizedTypeReference<VariableDto> SIMPLE = new ParameterizedTypeReference<>() {};
  private static final ParameterizedTypeReference<ResultsDto<VariableDto>> RESULTS = new ParameterizedTypeReference<>() {};

  public OperateVariablesEndpoint(
      RestTemplate restTemplate, URI operateEndpoint, CredentialsProvider credentialsProvider, ObjectMapper objectMapper
  ) {
    super(restTemplate, operateEndpoint, credentialsProvider, objectMapper);
  }

  @Override
  protected String endpointObjectType() {
    return "variables";
  }

  @Override
  protected ParameterizedTypeReference<VariableDto> simpleTypeReference() {
    return SIMPLE;
  }

  @Override
  protected ParameterizedTypeReference<ResultsDto<VariableDto>> resultsDtoTypeReference() {
    return RESULTS;
  }
}
