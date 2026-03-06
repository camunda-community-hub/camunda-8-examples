package com.camunda.example.client.zeebe.model;

import lombok.*;

import java.util.*;

@Data
public class InsuranceApplicationVariables {
  private Long age;
  private String vehicleManufacturer;
  private String vehicleModel;
  private RatingResult rating;
  private String applicationId;
}
