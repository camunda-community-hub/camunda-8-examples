package com.camunda.consulting;

public interface CompleteAction extends TaskAction {

  String getCompleter();

  void setCompleter(String completer);

}
