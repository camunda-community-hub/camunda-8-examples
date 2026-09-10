package com.camunda.example.client.operate;

import com.camunda.example.client.operate.model.*;
import com.fasterxml.jackson.databind.*;
import io.camunda.zeebe.client.*;
import org.springframework.core.*;
import org.springframework.web.client.*;

import java.net.*;

public class OperateIncidentsEndpoint extends AbstractOperateObjectEndpoint<IncidentDto>{
  private static final ParameterizedTypeReference<IncidentDto> SIMPLE = new ParameterizedTypeReference<>() {};
  private static final ParameterizedTypeReference<ResultsDto<IncidentDto>> RESULTS = new ParameterizedTypeReference<>() {};
  public OperateIncidentsEndpoint(
      RestTemplate restTemplate, URI operateEndpoint, CredentialsProvider credentialsProvider, ObjectMapper objectMapper
  ) {
    super(restTemplate, operateEndpoint, credentialsProvider, objectMapper);
  }

  @Override
  protected String endpointObjectType() {
    return "incidents";
  }

  @Override
  protected ParameterizedTypeReference<IncidentDto> simpleTypeReference() {
    return SIMPLE;
  }

  @Override
  protected ParameterizedTypeReference<ResultsDto<IncidentDto>> resultsDtoTypeReference() {
    return RESULTS;
  }
}
