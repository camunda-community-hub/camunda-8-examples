package com.camunda.example.client.operate.model;

import com.fasterxml.jackson.databind.node.*;
import lombok.*;

import java.util.*;

@Data
@Builder
public class QueryDto<T> {
  private T filter;
  private Long size;
  private Set<SortDto> sort;
  private ArrayNode searchAfter;
}
