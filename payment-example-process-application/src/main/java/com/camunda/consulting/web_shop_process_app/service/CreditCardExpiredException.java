package com.camunda.consulting.web_shop_process_app.service;

public class CreditCardExpiredException extends RuntimeException {

  public CreditCardExpiredException(String message) {
    super(message);
  }

}
