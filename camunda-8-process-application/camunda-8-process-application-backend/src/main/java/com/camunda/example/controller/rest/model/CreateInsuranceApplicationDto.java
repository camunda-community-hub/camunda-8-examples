package com.camunda.example.controller.rest.model;

import lombok.*;

@Data
public class CreateInsuranceApplicationDto {
  private Long age;
  private String name;
  private String email;
  private String vehicleManufacturer;
  private String vehicleModel;
}
