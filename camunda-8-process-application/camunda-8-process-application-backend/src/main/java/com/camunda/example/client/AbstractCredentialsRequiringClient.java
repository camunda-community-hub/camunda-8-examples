package com.camunda.example.client;

import com.fasterxml.jackson.databind.*;
import io.camunda.zeebe.client.*;
import io.grpc.*;
import lombok.*;
import org.slf4j.*;
import org.springframework.http.*;

import java.util.*;

public abstract class AbstractCredentialsRequiringClient {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final CredentialsProvider credentialsProvider;
  private final ObjectMapper objectMapper;

  protected AbstractCredentialsRequiringClient(CredentialsProvider credentialsProvider, ObjectMapper objectMapper) {
    this.credentialsProvider = credentialsProvider;
    this.objectMapper = objectMapper;
  }

  @SneakyThrows
  protected HttpHeaders createHeaders(List<MediaType> accept) {
    Metadata metadata = new Metadata();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(accept);
    credentialsProvider.applyCredentials(metadata);
    metadata
        .keys()
        .forEach(key -> {
          metadata
              .getAll(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER))
              .forEach(value -> {
                headers.add(key, value);
              });
        });
    return headers;
  }

  protected void logAsJson(String message, Object object) {
    try {
      log.debug(
          message,
          objectMapper
              .writerWithDefaultPrettyPrinter()
              .writeValueAsString(object)
      );
    } catch (Exception e) {
      log.debug(message, object);
    }
  }
}
