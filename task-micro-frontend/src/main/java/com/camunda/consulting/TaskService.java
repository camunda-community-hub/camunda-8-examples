package com.camunda.consulting;

import com.camunda.consulting.UpdateTaskDto.CompleteTaskDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.Form;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.exception.TaskListException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
  private final CamundaTaskListClient camundaTaskListClient;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public TaskService(CamundaTaskListClient camundaTaskListClient) {
    this.camundaTaskListClient = camundaTaskListClient;
  }

  public TaskDto getTask(String id) {
    try {
      Task task = camundaTaskListClient.getTask(id);
      Form form = camundaTaskListClient.getForm(task.getFormId(), task.getProcessDefinitionKey());
      return new TaskDto(
          task.getVariables().stream()
              .map(v -> Map.entry(v.getName(), v.getValue()))
              .collect(Collectors.toMap(Entry::getKey, Entry::getValue)),
          objectMapper.readValue(form.getSchema(), new TypeReference<Map<String, Object>>() {}), task.getTaskState().getRawValue());
    } catch (TaskListException | JsonProcessingException e) {
      throw new RuntimeException("Error while fetching task", e);
    }
  }

  public void handleUpdate(String id, UpdateTaskDto content) {
    switch (content) {
      case CompleteTaskDto complete:
        completeTask(id, complete);
    }
  }

  private void completeTask(String id, CompleteTaskDto content) {
    try {
      camundaTaskListClient.completeTask(id, content.result());
    } catch (TaskListException e) {
      throw new RuntimeException("Error while completing task", e);
    }
  }
}
