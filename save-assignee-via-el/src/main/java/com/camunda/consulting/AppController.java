package com.camunda.consulting;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.dto.TaskSearch;
import io.camunda.tasklist.exception.TaskListException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppController {
  private final CamundaTaskListClient taskListClient;

  @Autowired
  public AppController(CamundaTaskListClient taskListClient) {
    this.taskListClient = taskListClient;
  }

  @GetMapping("/tasks")
  public TaskList getTasks() throws TaskListException {
    return taskListClient.getTasks(new TaskSearch());
  }
}
