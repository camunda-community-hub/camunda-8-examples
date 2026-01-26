package com.camunda.consulting;

import com.camunda.consulting.UpdateTaskDto.Data.AssignTaskDto;
import com.camunda.consulting.UpdateTaskDto.Data.CompleteTaskDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.client.CamundaClient;
import io.camunda.client.api.search.response.SearchResponse;
import io.camunda.client.api.search.response.UserTask;
import io.camunda.client.api.search.response.Variable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
  private final CamundaClient camundaClient;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public TaskService(CamundaClient camundaClient) {
    this.camundaClient = camundaClient;
  }

  public TaskDto getTask(long key) {
    UserTask userTask = camundaClient.newUserTaskGetRequest(key).execute();
    io.camunda.client.api.search.response.Form formResponse =
        camundaClient.newUserTaskGetFormRequest(key).execute();
    Map<String, Object> variables = fetchAllVariables(key);
    return new TaskDto(variables, formResponse.getSchema(), userTask.getState().name());
  }

  private Map<String, Object> fetchAllVariables(long userTaskKey) {
    Map<String, Object> result = new HashMap<>();
    AtomicReference<String> cursor = new AtomicReference<>();
    boolean hasMore;
    do {
      SearchResponse<Variable> response =
          camundaClient
              .newUserTaskVariableSearchRequest(userTaskKey)
              .page(
                  cursor.get() == null ? p -> p.limit(100) : p -> p.limit(100).after(cursor.get()))
              .execute();
      cursor.set(response.page().endCursor());
      hasMore = response.items().isEmpty();
      response
          .items()
          .forEach(
              v -> {
                try {
                  result.put(
                      v.getName(),
                      objectMapper.readValue(
                          (v.isTruncated() ? fetchCompleteValue(v) : v).getValue(), Object.class));
                } catch (JsonProcessingException e) {
                  throw new RuntimeException(
                      "Error while parsing variable value " + v.getValue(), e);
                }
              });
    } while (hasMore);
    return result;
  }

  private Variable fetchCompleteValue(Variable v) {
    return camundaClient.newVariableGetRequest(v.getVariableKey()).execute();
  }

  public void handleUpdate(long key, UpdateTaskDto content) {
    switch (content.data()) {
      case CompleteTaskDto complete -> completeTask(key, complete);

      case AssignTaskDto assignTaskDto -> assignTask(key, assignTaskDto);
    }
  }

  private void assignTask(long key, AssignTaskDto assignTaskDto) {
    camundaClient.newAssignUserTaskCommand(key).assignee(assignTaskDto.assignee()).execute();
  }

  private void completeTask(long key, CompleteTaskDto content) {
    camundaClient.newCompleteUserTaskCommand(key).variables(content.result()).execute();
  }
}
