package com.camunda.consulting;

import com.camunda.consulting.AsyncService.TransactionResult;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import java.time.Duration;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class AsyncJobWorker {
  private static final String TRANSACTION_ID_VAR_NAME = "transactionId";

  private final ZeebeClient zeebeClient;
  private final AsyncService asyncService;

  @Autowired
  public AsyncJobWorker(ZeebeClient zeebeClient, AsyncService asyncService) {
    this.zeebeClient = zeebeClient;
    this.asyncService = asyncService;
  }

  @JobWorker(autoComplete = false, type = "async-job")
  public void handle(ActivatedJob job, @Variable(name = "transactionId") @Nullable String transactionId) {
    String txId = getOrCreateTransactionId(job, transactionId);
    asyncService.startTransaction(txId);
    TransactionResult result = asyncService.getTransactionResult(txId);
    if (result.complete()) {
      completeJob(job, result.result());
      // this comes afterward as it could be considered optional
      asyncService.completeTransaction(txId);
    } else {
      delayJob(job, Duration.ofSeconds(30));
    }
  }

  private void completeJob(ActivatedJob job, String result) {
    zeebeClient
        .newCompleteCommand(job)
        .variables(Collections.singletonMap("result", result))
        .send()
        .join();
  }

  private void delayJob(ActivatedJob job, Duration backoff) {
    zeebeClient.newFailCommand(job).retries(job.getRetries()).retryBackoff(backoff).send().join();
  }

  private String getOrCreateTransactionId(ActivatedJob job, @Nullable String existingId) {
    if (existingId == null) {
      // no transaction id present yet — create one and persist it to the job scope
      String transactionId = asyncService.createTransactionId();
      zeebeClient
          .newSetVariablesCommand(job.getElementInstanceKey())
          .variables(Collections.singletonMap(TRANSACTION_ID_VAR_NAME, transactionId))
          .local(true)
          .send()
          .join();
      return transactionId;
    }
    return existingId;
  }
}
