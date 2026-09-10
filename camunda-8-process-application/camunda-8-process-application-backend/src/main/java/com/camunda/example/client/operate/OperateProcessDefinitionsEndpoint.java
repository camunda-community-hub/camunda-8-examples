package com.camunda.example.client.operate;

import com.camunda.example.client.operate.model.*;
import com.fasterxml.jackson.databind.*;
import io.camunda.zeebe.client.*;
import io.github.resilience4j.retry.annotation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.*;
import org.springframework.http.*;
import org.springframework.http.client.*;
import org.springframework.stereotype.*;
import org.springframework.web.client.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class OperateProcessDefinitionsEndpoint extends AbstractOperateObjectEndpoint<ProcessDefinitionDto> {
  private static final ParameterizedTypeReference<ProcessDefinitionDto> SIMPLE = new ParameterizedTypeReference<>() {};
  private static final ParameterizedTypeReference<ResultsDto<ProcessDefinitionDto>> RESULTS = new ParameterizedTypeReference<>() {};
  public OperateProcessDefinitionsEndpoint(
      RestTemplate operateRestTemplate,
      URI operateEndpoint,
      CredentialsProvider credentialsProvider,
      ObjectMapper objectMapper
  ) {
    super(
        operateRestTemplate,
        operateEndpoint,
        credentialsProvider,
        objectMapper
    );
  }

  @Override
  protected String endpointObjectType() {
    return "process-definitions";
  }

  @Override
  protected ParameterizedTypeReference<ProcessDefinitionDto> simpleTypeReference() {
    return SIMPLE;
  }

  @Override
  protected ParameterizedTypeReference<ResultsDto<ProcessDefinitionDto>> resultsDtoTypeReference() {
    return RESULTS;
  }

  @Retry(name = "operate")
  public String xml(Long key) {
    return call(
        List.of(String.valueOf(key), "xml"),
        HttpMethod.GET,
        createEntity(Optional.empty(),List.of(MediaType.TEXT_XML)),
        new ParameterizedTypeReference<String>() {}
    ).getBody();
  }
}
