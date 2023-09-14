package com.camunda.consulting;

import com.camunda.consulting.AsyncService.TransactionResult;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import java.time.Duration;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
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
  public void handle(ActivatedJob job) {
    String transactionId = getOrCreateTransactionId(job);
    asyncService.startTransaction(transactionId);
    TransactionResult result = asyncService.getTransactionResult(transactionId);
    if (result.complete()) {
      completeJob(job, result.result());
      // this comes afterward as it could be considered optional
      asyncService.completeTransaction(transactionId);
    } else {
      delayJob(job, Duration.ofSeconds(30));
    }
  }

  /**
   * Completes the job with the given result
   *
   * @param job the job to complete
   * @param result the result to submit
   */
  private void completeJob(ActivatedJob job, String result) {
    zeebeClient
        .newCompleteCommand(job)
        .variables(Collections.singletonMap("result", result))
        .send()
        .join();
  }

  /**
   * Delays the job with the given backoff
   *
   * @param job the job to delay
   * @param backoff the backoff after which the job will be available again
   */
  private void delayJob(ActivatedJob job, Duration backoff) {
    zeebeClient.newFailCommand(job).retries(job.getRetries()).retryBackoff(backoff).send().join();
  }

  /**
   * Checks if a transaction id is already present and creates one if not
   *
   * @param job the job that requires the transaction id
   * @return the transaction id from the job or a created one that is saved to the job now
   */
  private String getOrCreateTransactionId(ActivatedJob job) {

    if (!job.getVariablesAsMap().containsKey(TRANSACTION_ID_VAR_NAME)) {
      // there is no transaction id present yet, better set one (and send it to zeebe)
      String transactionId = asyncService.createTransactionId();
      zeebeClient
          .newSetVariablesCommand(job.getElementInstanceKey())
          .variables(Collections.singletonMap(TRANSACTION_ID_VAR_NAME, transactionId))
          .local(true)
          .send()
          .join();
      return transactionId;
    }
    return (String) job.getVariablesAsMap().get(TRANSACTION_ID_VAR_NAME);
  }
}
