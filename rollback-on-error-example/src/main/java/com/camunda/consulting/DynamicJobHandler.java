package com.camunda.consulting;

import io.camunda.zeebe.client.api.response.ActivatedJob;

public interface DynamicJobHandler {
  String getJobTypeName();

  Object handle(ActivatedJob job);
}
