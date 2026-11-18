package com.camunda.example.controller.gql;

import com.camunda.example.client.tasklist.model.*;
import com.camunda.example.service.gql.*;
import com.fasterxml.jackson.databind.node.*;
import graphql.*;
import lombok.*;
import org.springframework.web.bind.annotation.*;

import java.io.*;

@RestController
@RequestMapping("graphql")
@RequiredArgsConstructor
public class GraphQLController {
  private final GraphQLService graphQLService;

  @PostMapping
  public GraphQLResponseDto<ObjectNode> graphql(@RequestBody GraphQLRequestDto requestDto) throws IOException {
    return graphQLService.executeQuery(requestDto);
  }
}
