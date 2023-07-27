package com.camunda.consulting;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;

public class UserTask {
  private long key;
  private long originalKey;
  private ObjectNode variables;
  private String taskName;
  private String elementId;
  private long elementInstanceKey;
  private List<String> rollbackTaskTypes;
  private long processInstanceKey;

  public long getOriginalKey() {
    return originalKey;
  }

  public void setOriginalKey(long originalKey) {
    this.originalKey = originalKey;
  }

  public long getProcessInstanceKey() {
    return processInstanceKey;
  }

  public void setProcessInstanceKey(long processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
  }

  public List<String> getRollbackTaskTypes() {
    return rollbackTaskTypes;
  }

  public void setRollbackTaskTypes(List<String> rollbackTaskTypes) {
    this.rollbackTaskTypes = rollbackTaskTypes;
  }

  public long getKey() {
    return key;
  }

  public void setKey(long key) {
    this.key = key;
  }

  public ObjectNode getVariables() {
    return variables;
  }

  public void setVariables(ObjectNode variables) {
    this.variables = variables;
  }

  public String getTaskName() {
    return taskName;
  }

  public void setTaskName(String taskName) {
    this.taskName = taskName;
  }

  public String getElementId() {
    return elementId;
  }

  public void setElementId(String elementId) {
    this.elementId = elementId;
  }

  public long getElementInstanceKey() {
    return elementInstanceKey;
  }

  public void setElementInstanceKey(long elementInstanceKey) {
    this.elementInstanceKey = elementInstanceKey;
  }
}
