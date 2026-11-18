package com.camunda.example.client.operate.model;

import com.fasterxml.jackson.databind.node.*;
import lombok.*;

import java.util.*;

@Data
public class ResultsDto<T> {
  private Set<T> items;
  private Long total;
  private ArrayNode sortValues;
}
