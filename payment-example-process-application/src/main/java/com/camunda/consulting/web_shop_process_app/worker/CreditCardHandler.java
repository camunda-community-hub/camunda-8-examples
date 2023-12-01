package com.camunda.consulting.web_shop_process_app.worker;

import com.camunda.consulting.web_shop_process_app.service.CreditCardExpiredException;
import com.camunda.consulting.web_shop_process_app.service.CreditCardService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreditCardHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CreditCardHandler.class);

  private CreditCardService creditCardService;

  public CreditCardHandler(CreditCardService creditCardService) {
    this.creditCardService = creditCardService;
  }

  @JobWorker(type = "creditCardCharging", autoComplete = false)
  public void handle(JobClient client, ActivatedJob job) {
    LOG.info("Handling credit card payment for process instance {}", job.getProcessInstanceKey());
    Map<String, Object> variables = job.getVariablesAsMap();
    String cardNumber = (String) variables.get("cardNumber");
    String cvc = (String) variables.get("cvc");
    String expiryDate = (String) variables.get("expiryDate");
    Double amount = Double.valueOf(variables.get("openAmount").toString());
    try {
      creditCardService.chargeAmount(cardNumber, cvc, expiryDate, amount);
      client.newCompleteCommand(job).send();
    } catch (CreditCardExpiredException e) {
      LOG.info("Credit card payment failed: {}", e.getLocalizedMessage());
      client
          .newThrowErrorCommand(job)
          .errorCode("creditCardError")
          .errorMessage(e.getLocalizedMessage())
          .variables(Map.of("errorMessage", e.getLocalizedMessage()))
          .send();
    }
  }
}
