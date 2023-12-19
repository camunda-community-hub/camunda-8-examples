package com.camunda.consulting.tasklist.service;

import com.camunda.consulting.tasklist.model.TaskDto;
import com.camunda.consulting.tasklist.model.TaskOverviewDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.Form;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskSearch;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.dto.Variable;
import io.camunda.tasklist.exception.TaskListException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
  private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);
  private final ObjectMapper objectMapper;
  private final CamundaTaskListClient camundaTaskListClient;

  public TaskService(
      ObjectMapper objectMapper,
      CamundaTaskListClient camundaTaskListClient) {
    this.objectMapper = objectMapper;
    this.camundaTaskListClient = camundaTaskListClient;
  }

  public TaskDto getTask(String id) {
    try {
      Task task = camundaTaskListClient.getTask(id);
      Form form = null;
      if (task.getFormKey().startsWith("camunda-forms:bpmn:")) {
        form = camundaTaskListClient.getForm(task.getFormKey(), task.getProcessDefinitionKey());
      }
      return map(task, form, task.getVariables());
    } catch (TaskListException e) {
      throw new RuntimeException("Error while fetching task", e);
    }
  }

  public List<TaskOverviewDto> getTasks() {
    try {
      return camundaTaskListClient.getTasks(new TaskSearch().setState(TaskState.CREATED)).getItems().stream()
                                  .map(this::map)
                                  .toList();
    } catch (TaskListException e) {
      throw new RuntimeException(e);
    }
  }

  private TaskOverviewDto map(Task task) {
    return new TaskOverviewDto(task.getId(), task.getName());
  }

  private TaskDto map(Task task, Form form, List<Variable> variables) {
    try {
      Map<String, Object> schema = null;
      if (form != null) {
        schema = objectMapper.readValue(form.getSchema(), new TypeReference<>() {});
      }
      Map<String, Object> data =
          variables.stream()
              .map(v -> Map.entry(v.getName(), v.getValue()))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      return new TaskDto(task.getId(), task.getName(), schema, data, task.getFormKey());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public void completeTask(String id, Map<String, Object> data) {
    try {
      camundaTaskListClient.completeTask(id, data);
    } catch (TaskListException e) {
      throw new RuntimeException(e);
    }
  }
}
