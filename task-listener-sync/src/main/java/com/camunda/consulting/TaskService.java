package com.camunda.consulting;

import com.camunda.consulting.InternalTask.State;
import com.camunda.consulting.InternalTask.SyncType;
import com.camunda.consulting.UpdateTaskDto.Data.AssignTaskDto;
import com.camunda.consulting.UpdateTaskDto.Data.CompleteTaskDto;
import io.camunda.client.CamundaClient;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.response.UserTaskProperties;
import io.camunda.client.api.search.enums.UserTaskState;
import io.camunda.client.api.search.request.SearchRequestPage;
import io.camunda.client.api.search.response.SearchResponse;
import io.camunda.client.api.search.response.UserTask;
import io.camunda.client.api.search.response.Variable;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class TaskService {
  private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);
  private final Map<Long, InternalTask> userTasks = new ConcurrentHashMap<>();
  private final Map<Long, Object> formsCache = new ConcurrentHashMap<>();
  private final CamundaClient camundaClient;
  private String cursor;

  public TaskService(CamundaClient camundaClient) {
    this.camundaClient = camundaClient;
  }

  private static Consumer<SearchRequestPage> page(String cursor) {
    return p -> {
      p.limit(100);
      if (cursor != null) {
        p.after(cursor);
      }
    };
  }

  public List<CustomUserTaskDto> getUserTasks() {
    return userTasks.entrySet().stream()
        .map(
            e ->
                new CustomUserTaskDto(
                    e.getKey(),
                    e.getValue().variables(),
                    e.getValue().formSupplier().get(),
                    e.getValue().state(),
                    e.getValue().syncType()))
        .toList();
  }

  @JobWorker(type = "custom:creating")
  public void onCreating(ActivatedJob activatedJob) {
    UserTaskProperties userTask = activatedJob.getUserTask();
    String taskSystem = activatedJob.getCustomHeaders().get("taskSystem");
    if (userTask != null && "custom".equals(taskSystem)) {
      putUserTask(
          userTask.getUserTaskKey(),
          activatedJob.getVariablesAsMap(),
          () -> getForm(userTask.getUserTaskKey(), 10, Duration.ofSeconds(1)),
          State.CREATED,
          SyncType.REACTIVE);
    }
  }

  @JobWorker(type = "custom:completing")
  public void onCompleting(ActivatedJob activatedJob) {
    UserTaskProperties userTask = activatedJob.getUserTask();
    String taskSystem = activatedJob.getCustomHeaders().get("taskSystem");
    if (userTask != null && "custom".equals(taskSystem)) {
      putUserTask(
          userTask.getUserTaskKey(),
          activatedJob.getVariablesAsMap(),
          () -> getForm(userTask.getUserTaskKey(), 10, Duration.ofSeconds(1)),
          State.COMPLETED,
          SyncType.REACTIVE);
    }
  }

  @JobWorker(type = "custom:canceling")
  public void onCanceling(ActivatedJob activatedJob) {
    UserTaskProperties userTask = activatedJob.getUserTask();
    String taskSystem = activatedJob.getCustomHeaders().get("taskSystem");
    if (userTask != null && "custom".equals(taskSystem)) {
      putUserTask(
          userTask.getUserTaskKey(),
          activatedJob.getVariablesAsMap(),
          () -> getForm(userTask.getUserTaskKey(), 10, Duration.ofSeconds(1)),
          State.CANCELED,
          SyncType.REACTIVE);
    }
  }

  @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.SECONDS)
  public void updateUserTasks() {
    SearchResponse<UserTask> activeUserTasks =
        camundaClient.newUserTaskSearchRequest().page(page(cursor)).execute();
    if (activeUserTasks.items().isEmpty()) {
      cursor = null;
    } else {
      syncUserTasks(activeUserTasks.items());
    }
  }

  private void syncUserTasks(List<UserTask> items) {
    items.forEach(this::syncUserTask);
  }

  private void syncUserTask(UserTask userTask) {
    if (!"custom".equals(userTask.getCustomHeaders().get("taskSystem"))) {
      return;
    }
    if (userTasks.containsKey(userTask.getUserTaskKey())) {
      if (List.of(State.COMPLETED, State.CANCELED)
          .contains(userTasks.get(userTask.getUserTaskKey()).state())) {
        return;
      }
      if (userTasks.get(userTask.getUserTaskKey()).syncType() == SyncType.POLLING) {
        putUserTask(
            userTask.getUserTaskKey(),
            getVariables(userTask.getUserTaskKey()),
            () -> getForm(userTask.getUserTaskKey(), 10, Duration.ofSeconds(1)),
            fromUserTaskState(userTask.getState()),
            SyncType.POLLING);
      }
    }
    if (!userTasks.containsKey(userTask.getUserTaskKey())) {
      putUserTask(
          userTask.getUserTaskKey(),
          getVariables(userTask.getUserTaskKey()),
          () -> getForm(userTask.getUserTaskKey(), 10, Duration.ofSeconds(1)),
          fromUserTaskState(userTask.getState()),
          SyncType.POLLING);
    }
  }

  private State fromUserTaskState(UserTaskState state) {
    return switch (state) {
      case FAILED, CREATED, CREATING, UPDATING, ASSIGNING -> State.CREATED;
      case COMPLETED, COMPLETING -> State.COMPLETED;
      case CANCELED, CANCELING -> State.CANCELED;
      case UNKNOWN_ENUM_VALUE ->
          throw new IllegalArgumentException("Unknown enum value cannot be handled");
    };
  }

  private Map<String, Object> getVariables(Long userTaskKey) {
    Map<String, Object> variables = new HashMap<>();
    camundaClient
        .newUserTaskVariableSearchRequest(userTaskKey)
        .execute()
        .items()
        .forEach(v -> variables.put(v.getName(), parseValue(v)));
    return variables;
  }

  private Object parseValue(Variable v) {
    if (v.isTruncated()) {
      return getVariable(v.getVariableKey());
    } else {
      return camundaClient.getConfiguration().getJsonMapper().fromJson(v.getValue(), Object.class);
    }
  }

  private Object getVariable(Long variableKey) {
    return camundaClient
        .getConfiguration()
        .getJsonMapper()
        .fromJson(
            camundaClient.newVariableGetRequest(variableKey).execute().getValue(), Object.class);
  }

  private Object getForm(Long userTaskKey, int retries, Duration retryDelay) {
    if (!formsCache.containsKey(userTaskKey)) {
      try {
        Object schema = camundaClient.newUserTaskGetFormRequest(userTaskKey).execute().getSchema();
        if (schema != null) {
          formsCache.put(userTaskKey, schema);
        }
      } catch (Exception e) {
        if (retries > 0) {
          try {
            Thread.sleep(retryDelay);
          } catch (InterruptedException ex) {
            throw new RuntimeException("Error while sleeping", ex);
          }
          return getForm(userTaskKey, retries - 1, retryDelay);
        }
        throw new RuntimeException("Error while loading form for user task " + userTaskKey, e);
      }
    }
    return formsCache.get(userTaskKey);
  }

  private void putUserTask(
      long userTaskKey,
      Map<String, Object> variables,
      Supplier<Object> formSupplier,
      State state,
      SyncType syncType) {
    LOG.info("{}: Syncing user task {} in state {}", userTaskKey, syncType, state);
    userTasks.put(userTaskKey, new InternalTask(variables, formSupplier, state, syncType));
  }

  public TaskDto getTask(long key) {
    if (userTasks.containsKey(key)) {
      InternalTask internalTask = userTasks.get(key);
      return new TaskDto(
          internalTask.variables(), internalTask.formSupplier().get(), internalTask.state());
    }
    throw new IllegalArgumentException("Did not find user task with key" + key);
  }

  public void handleUpdate(long id, UpdateTaskDto content) {
    switch (content.data()) {
      case CompleteTaskDto complete -> completeTask(id, complete);

      case AssignTaskDto assignTaskDto -> assignTask(id, assignTaskDto);
    }
  }

  private void assignTask(long id, AssignTaskDto assignTaskDto) {
    camundaClient.newAssignUserTaskCommand(id).assignee(assignTaskDto.assignee()).execute();
  }

  private void completeTask(long id, CompleteTaskDto content) {
    camundaClient.newCompleteUserTaskCommand(id).variables(content.result()).execute();
  }

  public record CustomUserTaskDto(
      long key, Map<String, Object> variables, Object form, State state, SyncType syncType) {}
}
