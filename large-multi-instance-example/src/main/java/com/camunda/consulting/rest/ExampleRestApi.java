package com.camunda.consulting.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

@RestController
public class ExampleRestApi {
  
  private static final Logger LOG = LoggerFactory.getLogger(ExampleRestApi.class);

  @Autowired
  ZeebeClient client;

  @PostMapping("/start-example")
  public ResponseEntity<String> startExampleProcess(@RequestBody String businessKey) {
    LOG.info("starting process with businessKey: {}", businessKey);
    ProcessInstanceEvent processInstanceEvent = client
        .newCreateInstanceCommand()
        .bpmnProcessId("campaignProcess")
        .latestVersion()
        .variables(businessKey)
        .send()
        .join();
    
    return ResponseEntity
        .status(HttpStatus.OK)
        .body("Started: " + processInstanceEvent.getProcessInstanceKey());
  }
}
