package com.camunda.consulting;

public interface BpmnErrorAction extends TaskAction {

  String getErrorCode();

  void setErrorCode(String errorCode);

  String getErrorMessage();

  void setErrorMessage(String errorMessage);

}
