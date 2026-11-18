package com.camunda.example.repository;

import com.camunda.example.repository.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface InsuranceApplicationRepository extends JpaRepository<InsuranceApplicationEntity, String> {}
