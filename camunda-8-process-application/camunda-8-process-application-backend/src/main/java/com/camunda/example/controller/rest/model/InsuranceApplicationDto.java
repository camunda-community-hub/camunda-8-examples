package com.camunda.example.controller.rest.model;

import lombok.*;

import java.util.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class InsuranceApplicationDto extends InsuranceApplicationIdDto{
  private String vehicleModel;
  private String vehicleManufacturer;
  private String applicantName;
  private Long applicantAge;
  private String processState;
  private String applicationState;
  private String email;
}
