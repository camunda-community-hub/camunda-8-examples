package com.camunda.consulting;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.ActivateJobsCommandStep1;
import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.command.FailJobCommandStep1;
import io.camunda.zeebe.client.api.command.FailJobCommandStep1.FailJobCommandStep2;
import io.camunda.zeebe.client.api.command.FinalCommandStep;
import io.camunda.zeebe.client.api.command.StreamJobsCommandStep1;
import io.camunda.zeebe.client.api.command.ThrowErrorCommandStep1;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.FailJobResponse;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.impl.ZeebeClientFutureImpl;
import io.camunda.zeebe.client.impl.command.CommandWithVariables;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass.FailJobRequest;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass.FailJobRequest.Builder;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RollbackJobHandler implements JobHandler {
  private static final Logger LOG = LoggerFactory.getLogger(RollbackJobHandler.class);
  private final JobHandler jobHandler;
  private final String rollbackElementId;
  private final String originalKeyVariableName;
  private final long originalKey;
  private final ZeebeClient zeebeClient;
  private final JsonMapper jsonMapper;
  Consumer<Exception> exceptionHandler;
  private Runnable closer;

  public RollbackJobHandler(
      JobHandler jobHandler,
      String rollbackElementId,
      String originalKeyVariableName,
      long originalKey,
      Consumer<Exception> exceptionHandler,
      ZeebeClient zeebeClient,
      JsonMapper jsonMapper) {
    this.jobHandler = jobHandler;
    this.rollbackElementId = rollbackElementId;
    this.originalKeyVariableName = originalKeyVariableName;
    this.originalKey = originalKey;
    this.exceptionHandler = exceptionHandler;
    this.zeebeClient = zeebeClient;
    this.jsonMapper = jsonMapper;
  }

  @Override
  public void handle(JobClient client, ActivatedJob job) throws Exception {
    Consumer<Exception> rollback =
        (e) -> {
          LOG.error(
              "Job completion for {} failed, rolling back to {}", job.getKey(), rollbackElementId);
          zeebeClient
              .newModifyProcessInstanceCommand(job.getProcessInstanceKey())
              .activateElement(rollbackElementId)
              .withVariables(Collections.singletonMap(originalKeyVariableName, originalKey))
              .and()
              .terminateElement(job.getElementInstanceKey())
              .send()
              .join();
          exceptionHandler.accept(e);
        };
    try {
      jobHandler.handle(new WrappedJobClient(client, rollback, jsonMapper), job);
    } catch (Exception e) {
      rollback.accept(e);
    } finally {
      LOG.info("Closing job handlers");
      closer.run();
    }
  }

  public void setCloser(Runnable closer) {
    this.closer = closer;
  }

  private static class WrappedJobClient implements JobClient {
    private final JobClient jobClient;
    private final Consumer<Exception> rollback;
    private final JsonMapper jsonMapper;

    private WrappedJobClient(
        JobClient jobClient, Consumer<Exception> rollback, JsonMapper jsonMapper) {
      this.jobClient = jobClient;
      this.rollback = rollback;
      this.jsonMapper = jsonMapper;
    }

    @Override
    public CompleteJobCommandStep1 newCompleteCommand(long jobKey) {
      return jobClient.newCompleteCommand(jobKey);
    }

    @Override
    public CompleteJobCommandStep1 newCompleteCommand(ActivatedJob job) {
      return jobClient.newCompleteCommand(job);
    }

    @Override
    public FailJobCommandStep1 newFailCommand(long jobKey) {
      return new WrappedFailJobCommandImpl(jsonMapper, jobKey, rollback);
    }

    @Override
    public FailJobCommandStep1 newFailCommand(ActivatedJob job) {
      return new WrappedFailJobCommandImpl(jsonMapper, job.getKey(), rollback);
    }

    @Override
    public ThrowErrorCommandStep1 newThrowErrorCommand(long jobKey) {
      return jobClient.newThrowErrorCommand(jobKey);
    }

    @Override
    public ThrowErrorCommandStep1 newThrowErrorCommand(ActivatedJob job) {
      return jobClient.newThrowErrorCommand(job);
    }

    @Override
    public ActivateJobsCommandStep1 newActivateJobsCommand() {
      return jobClient.newActivateJobsCommand();
    }

    @Override
    public StreamJobsCommandStep1 newStreamJobsCommand() {
      return jobClient.newStreamJobsCommand();
    }
  }

  private static class WrappedFailJobCommandImpl extends CommandWithVariables<FailJobCommandStep2>
      implements FailJobCommandStep1, FailJobCommandStep2 {
    private final Consumer<Exception> rollback;
    private final Builder builder;

    public WrappedFailJobCommandImpl(
        final JsonMapper jsonMapper, final long key, Consumer<Exception> rollback) {
      super(jsonMapper);
      this.rollback = rollback;
      builder = FailJobRequest.newBuilder();
      builder.setJobKey(key);
    }

    @Override
    public FailJobCommandStep2 retries(final int retries) {
      builder.setRetries(retries);
      return this;
    }

    @Override
    public FailJobCommandStep2 retryBackoff(final Duration backoffTimeout) {
      builder.setRetryBackOff(backoffTimeout.toMillis());
      return this;
    }

    @Override
    public FailJobCommandStep2 errorMessage(final String errorMsg) {
      builder.setErrorMessage(errorMsg);
      return this;
    }

    @Override
    public FailJobCommandStep2 setVariablesInternal(final String variables) {
      builder.setVariables(variables);
      return this;
    }

    @Override
    public FinalCommandStep<FailJobResponse> requestTimeout(final Duration requestTimeout) {
      return this;
    }

    @Override
    public ZeebeFuture<FailJobResponse> send() {
      final ZeebeClientFutureImpl<FailJobResponse, FailJobResponse> future =
          new ZeebeClientFutureImpl<>(r -> r);
      future.onNext(new FailJobResponse() {});
      CompletableFuture.runAsync(() -> rollback.accept(new FailJobException(builder)))
          .thenRun(future::onCompleted);
      return future;
    }
  }
}
