package com.camunda.consulting;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.camunda.client.CamundaClient;
import io.camunda.client.annotation.value.JobWorkerValue;
import io.camunda.client.api.JsonMapper;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobWorker;
import io.camunda.client.event.CamundaClientClosingEvent;
import io.camunda.client.event.CamundaClientCreatedEvent;
import io.camunda.client.jobhandling.CommandExceptionHandlingStrategy;
import io.camunda.client.jobhandling.JobExceptionHandlingStrategy;
import io.camunda.client.jobhandling.JobHandlerInvokingBeans;
import io.camunda.client.jobhandling.JobHandlingUtil;
import io.camunda.client.jobhandling.JobWorkerManager;
import io.camunda.client.jobhandling.parameter.ParameterResolver;
import io.camunda.client.jobhandling.parameter.ParameterResolverStrategy;
import io.camunda.client.jobhandling.result.ResultProcessor;
import io.camunda.client.jobhandling.result.ResultProcessorStrategy;
import io.camunda.client.metrics.MetricsRecorder;
import io.camunda.zeebe.model.bpmn.Bpmn;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class UserTaskService {
  private static final String ORIGINAL_KEY = "originalKey";
  private static final Logger LOG = LoggerFactory.getLogger(UserTaskService.class);
  private static final Duration REFRESH_DURATION = Duration.ofSeconds(15);
  private final Map<Long, UserTask> userTasks = new ConcurrentHashMap<>();
  private final Map<LocalDateTime, List<Long>> timeouts = new ConcurrentHashMap<>();
  private final CamundaClient zeebeClient;
  private final JobWorkerManager jobWorkerManager;
  private final CommandExceptionHandlingStrategy commandExceptionHandlingStrategy;
  private final JsonMapper jsonMapper;
  private final MetricsRecorder metricsRecorder;
  private final ParameterResolverStrategy parameterResolverStrategy;
  private final ResultProcessorStrategy resultProcessorStrategy;
  private final JobExceptionHandlingStrategy jobExceptionHandlingStrategy;
  private JobWorker userTaskWorker;

  public UserTaskService(
      CamundaClient zeebeClient,
      JobWorkerManager jobWorkerManager,
      CommandExceptionHandlingStrategy commandExceptionHandlingStrategy,
      JsonMapper jsonMapper,
      MetricsRecorder metricsRecorder,
      ParameterResolverStrategy parameterResolverStrategy,
      ResultProcessorStrategy resultProcessorStrategy,
      JobExceptionHandlingStrategy jobExceptionHandlingStrategy) {
    this.zeebeClient = zeebeClient;
    this.jobWorkerManager = jobWorkerManager;
    this.commandExceptionHandlingStrategy = commandExceptionHandlingStrategy;
    this.jsonMapper = jsonMapper;
    this.metricsRecorder = metricsRecorder;
    this.parameterResolverStrategy = parameterResolverStrategy;
    this.resultProcessorStrategy = resultProcessorStrategy;
    this.jobExceptionHandlingStrategy = jobExceptionHandlingStrategy;
  }

  private void add(UserTask userTask, Duration timeToLive) {
    removeTimedOutTasks();
    LOG.info("Adding task {}", userTask.getKey());
    userTasks.put(userTask.getKey(), userTask);
    userTasks.put(userTask.getOriginalKey(), userTask);
    timeouts
        .computeIfAbsent(LocalDateTime.now().plus(timeToLive), x -> new ArrayList<>())
        .addAll(Arrays.asList(userTask.getKey(), userTask.getOriginalKey()));
  }

  private void remove(Long key) {
    LOG.info("Removing task {}", key);
    userTasks.remove(key);
    timeouts.forEach((timestamp, keys) -> keys.remove(key));
  }

  public void complete(Long key, ObjectNode variables) {
    UserTask userTask = getUserTask(key);
    assert userTask != null;
    // create random job types for each future job
    Map<String, String> taskTypes =
        userTask.getRollbackTaskTypes().stream()
            .map(
                taskTypeVariableName ->
                    Map.entry(taskTypeVariableName, UUID.randomUUID().toString()))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    // add them to variables
    taskTypes.forEach(variables::put);
    Set<JobWorker> rollbackableWorkers = ConcurrentHashMap.newKeySet();
    AtomicReference<Exception> failedJobException = new AtomicReference<>();
    // define subscriptions for future tasks
    taskTypes.forEach(
        (jobTypeName, jobType) -> {
          // find the worker value by the jobTypeName (as the actual job type is dynamic)
          JobWorkerValue zeebeWorkerValue =
              jobWorkerManager.findJobWorkerConfigByType(jobTypeName).orElseThrow();
          // create the invoker for this worker value
          final List<ParameterResolver> parameterResolvers =
              JobHandlingUtil.createParameterResolvers(parameterResolverStrategy, zeebeWorkerValue);
          final ResultProcessor resultProcessor =
              JobHandlingUtil.createResultProcessor(resultProcessorStrategy, zeebeWorkerValue);
          JobHandlerInvokingBeans jobHandlerInvokingSpringBeans =
              new JobHandlerInvokingBeans(
                  zeebeWorkerValue,
                  commandExceptionHandlingStrategy,
                  metricsRecorder,
                  parameterResolvers,
                  resultProcessor,
                  jobExceptionHandlingStrategy);
          // wrap it to be able to react on the outcome
          RollbackJobHandler jobHandler =
              new RollbackJobHandler(
                  jobHandlerInvokingSpringBeans,
                  userTask.getElementId(),
                  getOriginalKeyVariableName(userTask),
                  userTask.getOriginalKey(),
                  failedJobException::set,
                  zeebeClient,
                  jsonMapper);

          JobWorker jobWorker =
              zeebeClient.newWorker().jobType(jobType).handler(jobHandler).name(jobTypeName).open();
          rollbackableWorkers.add(jobWorker);
          jobHandler.setCloser(
              () -> {
                if (failedJobException.get() != null) {
                  rollbackableWorkers.forEach(JobWorker::close);
                  rollbackableWorkers.clear();
                } else {
                  jobWorker.close();
                  rollbackableWorkers.remove(jobWorker);
                }
              });
        });
    // complete task via zeebe
    zeebeClient.newCompleteCommand(userTask.getKey()).variables(variables).send().join();
    while (!rollbackableWorkers.isEmpty()) {
      try {
        Thread.sleep(100L);
      } catch (InterruptedException ex) {
        // this is bad style
      }
    }
    remove(userTask.getKey());
    remove(userTask.getOriginalKey());
    if (failedJobException.get() != null) {
      throw new RuntimeException(
          "An exception happened while completing rollbackable tasks", failedJobException.get());
    }
  }

  public List<UserTask> getUserTasks() {
    removeTimedOutTasks();
    return userTasks.values().stream().distinct().toList();
  }

  public UserTask getUserTask(Long key) {
    removeTimedOutTasks();
    return userTasks.get(key);
  }

  private void removeTimedOutTasks() {
    timeouts.entrySet().stream()
        .filter(e -> e.getKey().isBefore(LocalDateTime.now()))
        .peek(e -> timeouts.remove(e.getKey()))
        .flatMap(e -> e.getValue().stream())
        .forEach(this::remove);
  }

  @EventListener(CamundaClientCreatedEvent.class)
  public void start() {
    userTaskWorker =
        zeebeClient
            .newWorker()
            .jobType("io.camunda.zeebe:userTask")
            .handler((client, job) -> add(createUserTask(job), REFRESH_DURATION.plusSeconds(10)))
            .timeout(REFRESH_DURATION)
            .name("UserTaskWorker")
            .open();
  }

  private UserTask createUserTask(ActivatedJob job) {
    UserTask userTask = new UserTask();
    userTask.setElementId(job.getElementId());
    userTask.setElementInstanceKey(job.getElementInstanceKey());
    userTask.setKey(job.getKey());
    userTask.setRollbackTaskTypes(
        Arrays.stream(job.getCustomHeaders().get("rollbackOnError").split(","))
            .map(String::trim)
            .toList());
    userTask.setTaskName(getTaskName(job.getElementId()));
    userTask.setVariables(job.getVariablesAsType(ObjectNode.class));
    userTask.setProcessInstanceKey(job.getProcessInstanceKey());
    userTask.setOriginalKey(
        (Long)
            job.getVariablesAsMap()
                .getOrDefault(getOriginalKeyVariableName(userTask), job.getKey()));
    return userTask;
  }

  private String getOriginalKeyVariableName(UserTask userTask) {
    return userTask.getElementId() + ":" + ORIGINAL_KEY;
  }

  private String getTaskName(String taskId) {
    try (InputStream in =
        getClass().getClassLoader().getResourceAsStream("rollback-process.bpmn")) {
      return Bpmn.readModelFromStream(in).getDocument().getElementById(taskId).getAttribute("name");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @EventListener(CamundaClientClosingEvent.class)
  public void stop() {
    if (userTaskWorker != null) {
      userTaskWorker.close();
      userTaskWorker = null;
    }
  }
}
