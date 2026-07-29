package com.camunda.example.client.operate.model;

import lombok.*;

@Data
public class SortDto {
  private String field;
  private OrderDto order;

  public enum OrderDto {
    ASC, DESC
  }
}
