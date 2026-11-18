package com.camunda.example.repository.model;

import lombok.*;

import javax.persistence.*;
import java.util.*;

@Entity
@Data
public class InsuranceApplicationEntity {
  @Id
  private String id;

  @Column(nullable = false)
  private Long processInstanceKey;
  @Column(nullable = false)
  private Long processDefinitionKey;
  @Column(nullable = false)
  private String applicantName;
  @Column(nullable = false)
  private String email;
  @Column(nullable = false)
  private String applicationState;
}
