package com.camunda.consulting.web_shop_process_app.rest;

import io.camunda.zeebe.client.ZeebeClient;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class StartFormRestController {
  private static final Logger LOG = LoggerFactory.getLogger(StartFormRestController.class);

  @Autowired private ZeebeClient zeebe;

  @PostMapping("/start")
  public void startProcessInstance(@RequestBody Map<String, Object> variables) {

    LOG.info("Starting process `paymentProcess` with variables: " + variables);

    zeebe
        .newCreateInstanceCommand()
        .bpmnProcessId("paymentProcess")
        .latestVersion()
        .variables(variables)
        .send()
        .join();
  }
}
