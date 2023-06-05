package org.camunda.community.examples.dmn.rest;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.camunda.community.examples.dmn.process.OnboardingProcessVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

@RestController
public class OnboardCustomerRestApi {

  @Autowired private ZeebeClient zeebeClient;

  @PutMapping("/customer")
  public ResponseEntity<String> startOnboarding(ServerWebExchange exchange) {
    // TODO: add data to the process instance from REST request
    String reference = startOnboarding("prepaid", 75, 10);

    // And just return something for the sake of the example
    return ResponseEntity.status(HttpStatus.OK).body("Started process instance " + reference);
  }

  public String startOnboarding(String paymentType, long customerRegionScore, long monthlyPayment) {
    OnboardingProcessVariables processVariables =
        new OnboardingProcessVariables()
            .setPaymentType(paymentType)
            .setCustomerRegionScore(customerRegionScore)
            .setMonthlyPayment(monthlyPayment);

    ProcessInstanceEvent processInstance =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("CustomerOnboarding")
            .latestVersion()
            .variables(processVariables)
            .send()
            .join(); // blocking call!

    return String.valueOf(processInstance.getProcessInstanceKey());
  }
}
