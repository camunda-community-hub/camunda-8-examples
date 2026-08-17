package com.camunda.consulting;

import com.camunda.consulting.impl.UserTask;

public interface TaskEventExporter {

  void exportTaskEvent(UserTask userTask);

}
