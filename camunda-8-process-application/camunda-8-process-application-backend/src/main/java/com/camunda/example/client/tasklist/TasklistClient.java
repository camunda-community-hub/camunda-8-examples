package com.camunda.example.client.tasklist;

import com.camunda.example.client.*;
import com.camunda.example.client.tasklist.model.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import io.camunda.zeebe.client.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.client.*;

import java.io.*;
import java.net.*;
import java.util.*;

@Component
@Slf4j
public class TasklistClient extends AbstractCredentialsRequiringClient {
  private final RestTemplate tasklistRestTemplate;
  private final URI tasklistUrl;

  @Autowired
  public TasklistClient(
      ObjectMapper objectMapper,
      @Qualifier("tasklist-rest-template") RestTemplate tasklistRestTemplate,
      @Qualifier("tasklist-url") URI tasklistUrl,
      @Qualifier("tasklist-credentials-provider") CredentialsProvider credentialsProvider
  ) {
    super(credentialsProvider, objectMapper);
    this.tasklistRestTemplate = tasklistRestTemplate;
    this.tasklistUrl = tasklistUrl;
  }

  private <T extends GraphQLResponseDto<?>> T executeQuery(
      GraphQLRequestDto requestDto, ParameterizedTypeReference<T> typeReference
  ) {
    logAsJson("Requesting from tasklist: \n{}", requestDto);

    HttpHeaders headers = createHeaders(List.of(MediaType.APPLICATION_JSON));

    HttpEntity<GraphQLRequestDto> graphQLRequestEntity = new HttpEntity<>(requestDto, headers);
    ResponseEntity<T> entity = tasklistRestTemplate.exchange(tasklistUrl,
        HttpMethod.POST,
        graphQLRequestEntity,
        typeReference
    );
    T body = entity.getBody();
    logAsJson("Response from tasklist: \n{}", body);
    return body;
  }

  public GraphQLResponseDto<ObjectNode> executeQuery(GraphQLRequestDto dto) {
    return executeQuery(dto, new ParameterizedTypeReference<>() {});
  }

}
