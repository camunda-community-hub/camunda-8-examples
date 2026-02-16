package com.camunda.consulting;

public interface TaskHandler {

  void completeTask(CompleteAction action);

  void throwError(BpmnErrorAction bpmnError);

  void correlateMessage(MessageAction action);

}
