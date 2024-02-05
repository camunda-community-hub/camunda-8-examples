package com.camunda.example.controller.zeebe;

import com.camunda.example.client.zeebe.model.*;
import com.camunda.example.service.business.*;
import io.camunda.zeebe.client.api.response.*;
import io.camunda.zeebe.client.api.worker.*;
import io.camunda.zeebe.spring.client.annotation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;

import javax.annotation.*;
import java.util.concurrent.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ZeebeController {
  private final InsuranceApplicationService service;
  private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1,
      10,
      10,
      TimeUnit.SECONDS,
      new ArrayBlockingQueue<>(50)
  );

  @PreDestroy
  public void shutdown() {
    threadPoolExecutor.shutdown();
    try {
      while (!threadPoolExecutor.awaitTermination(100, TimeUnit.SECONDS)) {
        log.info("There are more active jobs, shutdown will be delayed");
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @ZeebeWorker(type = "rejectApplication")
  public void rejectApplication(
      @ZeebeVariablesAsType InsuranceApplicationVariables insuranceApplicationVariables,
      JobClient client,
      ActivatedJob job
  ) {
    executeJob(() -> {
      log.info("Rejecting application with id {}", insuranceApplicationVariables.getApplicationId());
      if (!service.setApplicationState(insuranceApplicationVariables.getApplicationId(), "Rejected")) {
        throw new RuntimeException("Invalid applicationId " + insuranceApplicationVariables.getApplicationId());
      }
    }, client, job);
  }

  @ZeebeWorker(type = "issuePolicy")
  public void issuePolicy(
      @ZeebeVariablesAsType InsuranceApplicationVariables insuranceApplicationVariables,
      JobClient client,
      ActivatedJob job
  ) {
    executeJob(() -> {
      log.info("Issuing policy for application with id {}", insuranceApplicationVariables.getApplicationId());
      if (!service.setApplicationState(insuranceApplicationVariables.getApplicationId(), "Policy issued")) {
        throw new RuntimeException("Invalid applicationId " + insuranceApplicationVariables.getApplicationId());
      }
    }, client, job);
  }

  @ZeebeWorker(type = "sendRejection")
  public void sendRejection(
      @ZeebeVariablesAsType InsuranceApplicationVariables insuranceApplicationVariables,
      JobClient client,
      ActivatedJob job
  ) {
    executeJob(() -> {
      log.info("Sending rejection for application with id {}", insuranceApplicationVariables.getApplicationId());
      service
          .getEntity(insuranceApplicationVariables.getApplicationId())
          .map(entity -> {
            log.info("Sending rejection mail to {}", entity.getEmail());
            return entity;
          })
          .orElseThrow(() -> new RuntimeException("Invalid applicationId " + insuranceApplicationVariables.getApplicationId()));
    }, client, job);
  }

  @ZeebeWorker(type = "sendPolicy")
  public void sendPolicy(
      @ZeebeVariablesAsType InsuranceApplicationVariables insuranceApplicationVariables,
      JobClient client,
      ActivatedJob job
  ) {
    executeJob(() -> {
      log.info("Sending policy for application with id {}", insuranceApplicationVariables.getApplicationId());
      service
          .getEntity(insuranceApplicationVariables.getApplicationId())
          .map(entity -> {
            log.info("Sending policy to {}", entity.getEmail());
            return entity;
          })
          .orElseThrow(() -> new RuntimeException("Invalid applicationId " + insuranceApplicationVariables.getApplicationId()));
    }, client, job);
  }

  private void executeJob(
      Runnable runner, JobClient client, ActivatedJob job
  ) {
    try {
      threadPoolExecutor.submit(() -> {
        try {
          runner.run();
          client
              .newCompleteCommand(job)
              .send();
        } catch (Exception e) {
          client
              .newFailCommand(job)
              .retries(job.getRetries() - 1)
              .errorMessage(e.getMessage())
              .send();
        }
      });
    } catch (RejectedExecutionException e) {
      client.newFailCommand(job).retries(job.getRetries()).errorMessage(e.getMessage()).send();
    }
  }

}
