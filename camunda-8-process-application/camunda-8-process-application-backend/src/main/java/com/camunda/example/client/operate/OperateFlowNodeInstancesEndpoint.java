package com.camunda.example.client.operate;

import com.camunda.example.client.operate.model.*;
import com.fasterxml.jackson.databind.*;
import io.camunda.zeebe.client.*;
import org.springframework.core.*;
import org.springframework.web.client.*;

import java.net.*;

public class OperateFlowNodeInstancesEndpoint extends AbstractOperateObjectEndpoint<FlowNodeInstanceDto> {
  private static final ParameterizedTypeReference<FlowNodeInstanceDto> SIMPLE = new ParameterizedTypeReference<>() {};
  private static final ParameterizedTypeReference<ResultsDto<FlowNodeInstanceDto>> RESULTS = new ParameterizedTypeReference<>() {};

  public OperateFlowNodeInstancesEndpoint(
      RestTemplate restTemplate, URI operateEndpoint, CredentialsProvider credentialsProvider, ObjectMapper objectMapper
  ) {
    super(restTemplate, operateEndpoint, credentialsProvider, objectMapper);
  }

  @Override
  protected String endpointObjectType() {
    return "flownode-instances";
  }

  @Override
  protected ParameterizedTypeReference<FlowNodeInstanceDto> simpleTypeReference() {
    return SIMPLE;
  }

  @Override
  protected ParameterizedTypeReference<ResultsDto<FlowNodeInstanceDto>> resultsDtoTypeReference() {
    return RESULTS;
  }
}
