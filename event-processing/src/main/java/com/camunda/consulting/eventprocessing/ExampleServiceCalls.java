package com.camunda.consulting.eventprocessing;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExampleServiceCalls {
  private static final Logger LOG = LoggerFactory.getLogger(ExampleServiceCalls.class);

  @JobWorker
  public void service1() {
    LOG.info("Service 1 called");
  }

  @JobWorker
  public void service2() {
    LOG.info("Service 2 called");
  }

  @JobWorker
  public void service3() {
    LOG.info("Service 3 called");
  }
}
