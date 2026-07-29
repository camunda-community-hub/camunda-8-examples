package com.camunda.example.client.operate;

import com.camunda.example.client.operate.model.*;
import com.fasterxml.jackson.databind.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.http.client.*;
import org.springframework.web.client.*;

import java.io.*;
import java.net.*;
import java.util.*;

@RequiredArgsConstructor
public class OperateResponseErrorHandler extends DefaultResponseErrorHandler {
  private final ObjectMapper objectMapper;

  @Override
  public void handleError(ClientHttpResponse response) throws IOException {
    throw objectMapper.readValue(response
        .getBody()
        .readAllBytes(), ErrorDto.class);
  }

}
