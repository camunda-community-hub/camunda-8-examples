package com.camunda.consulting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task")
public class TaskController {
  private static final Logger LOG = LoggerFactory.getLogger(TaskController.class);
  private final TaskService taskService;

  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  @GetMapping(value = "/{id}", produces = "*/*")
  public Resource homePage(@PathVariable(name = "id") String id) {
    LOG.info("Loading task {}", id);
    return new ClassPathResource("index.html");
  }

  @GetMapping(value = "/{id}", produces = "application/json")
  public TaskDto getTask(@PathVariable(name = "id") long id) {
    return taskService.getTask(id);
  }

  @PatchMapping(value = "/{id}")
  public void updateTask(@PathVariable(name = "id") long id, @RequestBody UpdateTaskDto request) {
    taskService.handleUpdate(id, request);
  }
}
