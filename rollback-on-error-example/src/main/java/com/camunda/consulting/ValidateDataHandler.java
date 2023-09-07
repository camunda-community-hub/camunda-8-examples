package com.camunda.consulting;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.springframework.stereotype.Component;

@Component
public class ValidateDataHandler implements DynamicJobHandler {
  @Override
  public String getJobTypeName() {
    return "validateDataType";
  }

  @Override
  public Object handle(ActivatedJob job) {
    return null;
  }
}
