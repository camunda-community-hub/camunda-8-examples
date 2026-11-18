package com.camunda.consulting.impl;

import com.camunda.consulting.CompleteAction;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CompletedTaskMessage implements CompleteAction {

  private String id;
  private Map<String, Object> variables;
  private String completer;

}
