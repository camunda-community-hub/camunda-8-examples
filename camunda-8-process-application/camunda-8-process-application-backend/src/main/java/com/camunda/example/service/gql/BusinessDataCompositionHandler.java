package com.camunda.example.service.gql;

import com.camunda.example.repository.InsuranceApplicationRepository;
import com.camunda.example.repository.model.InsuranceApplicationEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

@Component
@Slf4j
public class BusinessDataCompositionHandler extends AbstractVariableAppender {
  public BusinessDataCompositionHandler(
      ObjectMapper objectMapper, InsuranceApplicationRepository insuranceApplicationRepository
  ) {
    super(objectMapper, insuranceApplicationRepository);
  }

  @Override
  protected Set<Function<InsuranceApplicationEntity, Entry<String, JsonNode>>> variablesMappers(ObjectMapper objectMapper) {
    return Set.of(entity -> Map.entry("applicantName",
        objectMapper.valueToTree(entity.getApplicantName())
    ));
  }

}
