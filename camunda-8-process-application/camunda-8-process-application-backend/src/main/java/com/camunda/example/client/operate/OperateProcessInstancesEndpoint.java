package com.camunda.example.client.operate;

import com.camunda.example.client.operate.model.*;
import com.fasterxml.jackson.databind.*;
import io.camunda.zeebe.client.*;
import io.github.resilience4j.retry.annotation.*;
import org.springframework.core.*;
import org.springframework.http.*;
import org.springframework.web.client.*;

import java.net.*;
import java.util.*;

public class OperateProcessInstancesEndpoint extends AbstractOperateObjectEndpoint<ProcessInstanceDto> {
  private static final ParameterizedTypeReference<ProcessInstanceDto> SIMPLE = new ParameterizedTypeReference<>() {};
  private static final ParameterizedTypeReference<ResultsDto<ProcessInstanceDto>> RESULTS = new ParameterizedTypeReference<>() {};

  public OperateProcessInstancesEndpoint(
      RestTemplate restTemplate, URI operateEndpoint, CredentialsProvider credentialsProvider, ObjectMapper objectMapper
  ) {
    super(restTemplate, operateEndpoint, credentialsProvider, objectMapper);
  }

  @Override
  protected String endpointObjectType() {
    return "process-instances";
  }

  @Override
  protected ParameterizedTypeReference<ProcessInstanceDto> simpleTypeReference() {
    return SIMPLE;
  }

  @Override
  protected ParameterizedTypeReference<ResultsDto<ProcessInstanceDto>> resultsDtoTypeReference() {
    return RESULTS;
  }

  @Retry(name = "operate")
  public ChangeStatusDto delete(Long key) {
    return call(
        List.of(String.valueOf(key)),
        HttpMethod.DELETE,
        createEntity(Optional.empty(),List.of(MediaType.APPLICATION_JSON)),
        new ParameterizedTypeReference<ChangeStatusDto>() {}
    ).getBody();
  }
}
