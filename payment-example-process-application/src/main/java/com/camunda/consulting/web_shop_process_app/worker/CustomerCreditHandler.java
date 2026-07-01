package com.camunda.consulting.web_shop_process_app.worker;

import com.camunda.consulting.web_shop_process_app.service.CustomerService;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.annotation.Variable;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomerCreditHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CustomerCreditHandler.class);

  private final CustomerService customerService;

  public CustomerCreditHandler(CustomerService customerService) {
    this.customerService = customerService;
  }

  @JobWorker
  public Map<String, Object> customerCreditHandling(
      @Variable String customerId, @Variable Double orderTotal) {
    LOG.info("Handling customer credit for customerId {}", customerId);

    Double customerCredit = customerService.getCustomerCredit(customerId);
    Double remainingAmount = customerService.deductCredit(customerId, orderTotal, customerCredit);

    return Map.of("customerCredit", customerCredit, "remainingAmount", remainingAmount);
  }
}
