package com.camunda.consulting;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.springframework.stereotype.Component;

@Component
public class SaveDataHandler implements DynamicJobHandler {
  @Override
  public String getJobTypeName() {
    return "saveDataType";
  }

  @Override
  public Object handle(ActivatedJob job) {
    throw new RuntimeException("An error happened");
  }
}
