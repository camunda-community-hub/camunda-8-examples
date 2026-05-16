package com.camunda.consulting.listener;

public class CannotCompleteTaskException extends Exception {

  public CannotCompleteTaskException(String message) {
    super(message);
  }
}
