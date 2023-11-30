package com.camunda.consulting.web_shop_process_app.worker;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.camunda.consulting.web_shop_process_app.service.CustomerService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;

@Component
public class CustomerCreditHandler {
  
  private static final Logger LOG = LoggerFactory.getLogger(CustomerCreditHandler.class);
  
  CustomerService customerService;

  public CustomerCreditHandler(CustomerService customerService) {
    super();
    this.customerService = customerService;
  }

  @JobWorker(type = "customerCreditHandling")
  public Map<String, Object> handle(ActivatedJob job) {
    LOG.info("Handling customer credit for process instance {}", job.getProcessInstanceKey());
        
    Map<String,Object> variables = job.getVariablesAsMap();
    String customerId = (String) variables.get("customerId");
    Double amount = Double.valueOf(variables.get("orderTotal").toString());
    
    Double customerCredit = customerService.getCustomerCredit(customerId);
    Double remainingAmount = customerService.deductCredit(customerId, amount, customerCredit);
    
    return Map.of("customerCredit", customerCredit,
        "remainingAmount", remainingAmount);
  }

}
