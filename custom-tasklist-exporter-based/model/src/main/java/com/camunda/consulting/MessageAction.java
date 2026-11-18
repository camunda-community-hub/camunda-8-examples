package com.camunda.consulting;

public interface MessageAction extends TaskAction {

  String getMessageName();

  void setMessageName();

  String getCorrelationKey();

  void setCorrelationKey();

}
