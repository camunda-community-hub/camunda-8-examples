package com.camunda.consulting.impl;

import com.camunda.consulting.EventType;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "UserTasks")
public class UserTask {

  // required
  @Id
  @Field("_id")
  protected String userTaskId;
  protected String source;
  protected EventType eventType;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String taskElementName;
  // optional
  protected String jobKey;
  protected String formKey;
  protected String assignee;
  protected List<String> candidateGroups;
  protected List<String> candidateUsers;
  protected String dueDate;
  protected String followUpDate;
  protected int priority;
  protected Map<String, Object> variables;

}
