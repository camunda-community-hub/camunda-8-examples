package com.camunda.consulting.zeebe;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import java.util.logging.Logger;


public class UserTaskHandler implements JobHandler {

  private final Logger LOGGER = Logger.getLogger(UserTaskHandler.class.getName());

  @Override
  public void handle(JobClient client, ActivatedJob job) throws Exception {
    LOGGER.finest("User Task received: " + job);
  }
}
