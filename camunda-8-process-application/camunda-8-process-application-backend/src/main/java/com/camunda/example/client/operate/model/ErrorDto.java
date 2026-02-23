package com.camunda.example.client.operate.model;

import lombok.*;

@Getter
@Setter
public class ErrorDto extends RuntimeException{
  private String status;
  private String message;
  private String instance;
  private String type;
}
