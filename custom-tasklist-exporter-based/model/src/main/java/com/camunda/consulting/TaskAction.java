package com.camunda.consulting;

import java.util.Map;

public interface TaskAction {

  String getId();

  void setId(String id);

  Map<String, Object> getVariables();

  void setVariables(Map<String, Object> variables);

}
