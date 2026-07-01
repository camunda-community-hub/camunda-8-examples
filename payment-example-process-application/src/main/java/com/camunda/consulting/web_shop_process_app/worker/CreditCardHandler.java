package com.camunda.consulting.web_shop_process_app.worker;

import com.camunda.consulting.web_shop_process_app.service.CreditCardExpiredException;
import com.camunda.consulting.web_shop_process_app.service.CreditCardService;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.annotation.Variable;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreditCardHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CreditCardHandler.class);

  private final CreditCardService creditCardService;

  public CreditCardHandler(CreditCardService creditCardService) {
    this.creditCardService = creditCardService;
  }

  @JobWorker(autoComplete = false)
  public void creditCardCharging(
      JobClient client,
      ActivatedJob job,
      @Variable String cardNumber,
      @Variable String cvc,
      @Variable String expiryDate,
      @Variable Double openAmount) {
    LOG.info("Handling credit card payment for process instance {}", job.getProcessInstanceKey());
    try {
      creditCardService.chargeAmount(cardNumber, cvc, expiryDate, openAmount);
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
