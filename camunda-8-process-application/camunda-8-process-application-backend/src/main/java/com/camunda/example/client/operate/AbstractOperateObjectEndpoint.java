package com.camunda.example.client.operate;

import com.camunda.example.client.*;
import com.camunda.example.client.operate.model.*;
import com.fasterxml.jackson.databind.*;
import io.camunda.zeebe.client.*;
import io.github.resilience4j.retry.annotation.*;
import lombok.extern.slf4j.*;
import org.springframework.core.*;
import org.springframework.http.*;
import org.springframework.web.client.*;
import org.springframework.web.util.*;

import java.net.*;
import java.util.*;

@Slf4j
public abstract class AbstractOperateObjectEndpoint<T> extends AbstractCredentialsRequiringClient
    implements OperateObjectEndpoint<T> {
  public AbstractOperateObjectEndpoint(
      RestTemplate restTemplate, URI operateEndpoint, CredentialsProvider credentialsProvider, ObjectMapper objectMapper
  ) {
    super(credentialsProvider, objectMapper);
    this.restTemplate = restTemplate;
    this.operateEndpoint = operateEndpoint;
  }

  private final RestTemplate restTemplate;

  private final URI operateEndpoint;

  @Override
  @Retry(name = "operate")
  public ResultsDto<T> search(QueryDto<T> query) {
    return call(List.of("search"),
        HttpMethod.POST,
        createEntity(Optional.of(query), List.of(MediaType.APPLICATION_JSON)),
        resultsDtoTypeReference()
    ).getBody();
  }

  @Override
  @Retry(name = "operate")
  public T get(Long key) {
    return call(List.of(String.valueOf(key)),
        HttpMethod.GET,
        createEntity(Optional.empty(),List.of(MediaType.APPLICATION_JSON)),
        simpleTypeReference()
    ).getBody();
  }

  protected <Req, Res> ResponseEntity<Res> call(
      List<String> pathSegments,
      HttpMethod httpMethod,
      HttpEntity<Req> httpEntity,
      ParameterizedTypeReference<Res> typeReference
  ) {
    return restTemplate.exchange(createEndpoint(pathSegments.toArray(new String[0])),
        httpMethod,
        httpEntity,
        typeReference
    );
  }

  protected abstract String endpointObjectType();

  protected abstract ParameterizedTypeReference<T> simpleTypeReference();

  protected abstract ParameterizedTypeReference<ResultsDto<T>> resultsDtoTypeReference();

  protected URI createEndpoint(String... pathSegments) {
    return UriComponentsBuilder
        .fromUri(operateEndpoint)
        .pathSegment(endpointObjectType())
        .pathSegment(pathSegments)
        .build()
        .toUri();
  }

  protected <B> HttpEntity<B> createEntity(Optional<B> body, List<MediaType> accept) {
    return body
        .map(t -> new HttpEntity<>(t, createHeaders(accept)))
        .orElseGet(() -> new HttpEntity<>(createHeaders(accept)));
  }

}
