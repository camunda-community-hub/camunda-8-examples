package com.camunda.consulting.api.graphql;

import com.camunda.consulting.EventType;
import com.camunda.consulting.impl.UserTask;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

public class UserTaskDTO {

  private String userTaskId;
  private String processInstanceId;

  private String processDefinitionId;
  private String taskElementName;
  private String formKey;
  private String assignee;
  private List<String> candidateGroups;
  private List<String> candidateUsers;
  private String dueDate;
  private String followUpDate;
  private int priority;
  private String variables;
  private String source;
  private EventType eventType;
  private ObjectMapper mapper = new ObjectMapper();

  public UserTaskDTO(UserTask userTask) {
    this.userTaskId = userTask.getUserTaskId();
    this.processInstanceId = userTask.getProcessInstanceId();
    this.processDefinitionId = userTask.getProcessDefinitionId();
    this.taskElementName = userTask.getTaskElementName();
    this.formKey = userTask.getFormKey();
    this.assignee = userTask.getAssignee();
    this.candidateGroups = userTask.getCandidateGroups();
    this.candidateUsers = userTask.getCandidateUsers();
    this.dueDate = userTask.getDueDate();
    this.followUpDate = userTask.getFollowUpDate();
    this.priority = userTask.getPriority();
    try {
      this.variables = mapper.writeValueAsString(userTask.getVariables());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    this.source = userTask.getSource();
    this.eventType = userTask.getEventType();
  }

  public String getUserTaskId() {
    return userTaskId;
  }

  public void setUserTaskId(String userTaskId) {
    this.userTaskId = userTaskId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getTaskElementName() {
    return taskElementName;
  }

  public void setTaskElementName(String taskElementName) {
    this.taskElementName = taskElementName;
  }

  public String getFormKey() {
    return formKey;
  }

  public void setFormKey(String formKey) {
    this.formKey = formKey;
  }

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  public List<String> getCandidateGroups() {
    return candidateGroups;
  }

  public void setCandidateGroups(List<String> candidateGroups) {
    this.candidateGroups = candidateGroups;
  }

  public List<String> getCandidateUsers() {
    return candidateUsers;
  }

  public void setCandidateUsers(List<String> candidateUsers) {
    this.candidateUsers = candidateUsers;
  }

  public String getDueDate() {
    return dueDate;
  }

  public void setDueDate(String dueDate) {
    this.dueDate = dueDate;
  }

  public String getFollowUpDate() {
    return followUpDate;
  }

  public void setFollowUpDate(String followUpDate) {
    this.followUpDate = followUpDate;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public String getVariables() {
    return variables;
  }

  public void setVariables(String variables) {
    this.variables = variables;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public EventType getEventType() {
    return eventType;
  }

  public void setEventType(EventType eventType) {
    this.eventType = eventType;
  }

  @Override
  public String toString() {
    return "UserTaskDTO{" +
        "userTaskId='" + userTaskId + '\'' +
        ", processInstanceId='" + processInstanceId + '\'' +
        ", taskElementName='" + taskElementName + '\'' +
        ", formKey='" + formKey + '\'' +
        ", assignee='" + assignee + '\'' +
        ", candidateGroups=" + candidateGroups +
        ", candidateUsers=" + candidateUsers +
        ", dueDate='" + dueDate + '\'' +
        ", followUpDate='" + followUpDate + '\'' +
        ", priority=" + priority +
        ", variables=" + variables +
        ", source='" + source + '\'' +
        ", eventType=" + eventType +
        '}';
  }
}
