package com.camunda.consulting.api.controller;

import com.camunda.consulting.EventType;
import com.camunda.consulting.api.graphql.UserTaskDTO;
import com.camunda.consulting.api.service.UserTaskService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class UserTaskController {

  @Autowired
  private UserTaskService userTaskService;

  @QueryMapping
  public Optional<UserTaskDTO> userTaskById(@Argument String userTaskId) {
    return userTaskService.userTaskByIdDto(userTaskId);
  }

  @QueryMapping
  public List<UserTaskDTO> allUserTasks(@Argument Optional<Integer> page,
      @Argument Optional<Integer> size) {
    return userTaskService.allUserTasks(page, size);
  }

  @QueryMapping
  public List<UserTaskDTO> filteredUserTasks(
      @Argument Optional<String> userTaskId,
      @Argument Optional<String> processInstanceId,
      @Argument Optional<String> taskElementName,
      @Argument Optional<String> formKey,
      @Argument Optional<String> assignee,
      @Argument List<String> candidateUsers,
      @Argument List<String> candidateGroups,
      @Argument Optional<String> dueDate,
      @Argument Optional<String> followUpDate,
      @Argument Optional<Integer> priority,
      @Argument Optional<String> source,
      @Argument Optional<EventType> eventType,
      @Argument Optional<Integer> page,
      @Argument Optional<Integer> size) {

    return userTaskService.filteredUserTasks(userTaskId, processInstanceId, taskElementName,
        formKey, assignee, candidateUsers, candidateGroups, dueDate, followUpDate, priority, source, eventType, page, size);
  }

  @MutationMapping
  public UserTaskDTO completeUserTask(@Argument String userTaskId,
      @Argument String variables) {
    return userTaskService.completeUserTask(userTaskId, variables);
  }

  @MutationMapping
  public UserTaskDTO assignUserTask(@Argument String userTaskId,
      @Argument String assignee) {
    return userTaskService.assignUserTask(userTaskId, assignee);
  }
}