package com.camunda.consulting;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.dto.TaskSearch;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.tasklist.generated.model.TaskByVariables;
import io.camunda.tasklist.generated.model.TaskByVariables.OperatorEnum;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserTaskExecutionListener {
  private static final Logger LOG = LoggerFactory.getLogger(UserTaskExecutionListener.class);
  private final CamundaTaskListClient taskListClient;

  @Autowired
  public UserTaskExecutionListener(CamundaTaskListClient taskListClient) {
    this.taskListClient = taskListClient;
  }

  @JobWorker(autoComplete = false)
  public void saveAssignee(JobClient jobClient, ActivatedJob job) throws TaskListException {
    String uniqueTaskKey = (String) job.getVariablesAsMap().get("uniqueTaskKey");
    long processInstanceKey = job.getProcessInstanceKey();
    String elementId = job.getElementId();
    long elementInstanceKey = job.getElementInstanceKey();
    LOG.info("Handling assignee for user task {}", elementInstanceKey);
    TaskList tasks =
        taskListClient.getTasks(
            new TaskSearch()
                .setProcessInstanceKey(String.valueOf(processInstanceKey))
                .setTaskDefinitionId(elementId)
                .setTaskVariables(
                    List.of(
                        new TaskByVariables()
                            .name("uniqueTaskKey")
                            // the escaping is a workaround until this is fixed
                            .value('"' + uniqueTaskKey + '"')
                            .operator(OperatorEnum.EQ)))
                .setState(TaskState.COMPLETED));
    Task matchingTask = extractMatchingTask(tasks);
    if (matchingTask != null) {
      LOG.info("Found one matching user task, extracting assignee");
      jobClient
          .newCompleteCommand(job.getKey())
          .variables(new AssignmentInformation(matchingTask.getAssignee()))
          .send()
          .join();
      LOG.info("Set assignee to process instance");
    } else {
      LOG.warn("Fond no matching user task, retrying in 5 seconds");
      jobClient
          .newFailCommand(job.getKey())
          .retries(job.getRetries() - 1)
          .retryBackoff(Duration.ofSeconds(5))
          .send()
          .join();
    }
  }

  private Task extractMatchingTask(TaskList tasks) {
    if (tasks.getItems().isEmpty()) {
      LOG.info("No tasks present");
      return null;
    }
    if (tasks.getItems().size() > 1) {
      // this will only happen if the same task in the same process instance gets the same randomly
      // generated uniqueTaskKey
      throw new IllegalStateException("Found more than one matching task");
    }
    return tasks.get(0);
  }

  public record AssignmentInformation(String assignee) {}
}
