package com.camunda.consulting;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task")
public class UserTaskController {
  private final UserTaskService service;

  public UserTaskController(UserTaskService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<List<UserTask>> getTasks() {
    return ResponseEntity.ok(service.getUserTasks());
  }

  @PostMapping("/{key}/complete")
  public ResponseEntity<String> completeTask(
      @PathVariable("key") Long key, @RequestBody ObjectNode variables) {
    try {
      service.complete(key, variables);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(e.getMessage());
    }
  }
}
