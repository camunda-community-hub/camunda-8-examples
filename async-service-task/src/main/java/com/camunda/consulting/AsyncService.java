package com.camunda.consulting;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AsyncService {
  private static final Logger LOG = LoggerFactory.getLogger(AsyncService.class);
  private final Map<String, LocalDateTime> transactions = new HashMap<>();

  /**
   * Creates a transactionId that is guaranteed to be available
   *
   * @return the transactionId
   */
  public String createTransactionId() {
    String transactionId = UUID.randomUUID().toString();
    while (transactions.containsKey(transactionId)) {
      transactionId = UUID.randomUUID().toString();
    }
    return transactionId;
  }

  /**
   * Starts a transaction for the given id if not already started
   *
   * @param transactionId the id that identifies the transaction
   */
  public void startTransaction(String transactionId) {
    if (transactions.containsKey(transactionId)) {
      LOG.info("Transaction '{}' already started, request will be ignored", transactionId);
    } else {
      LOG.info("Creating transaction '{}'", transactionId);
      transactions.put(transactionId, LocalDateTime.now().plusMinutes(1L));
    }
  }

  /**
   * Returns the current state of a running transaction
   *
   * @param transactionId the id that identifies the transaction
   * @return the transaction result
   * @throws IllegalStateException if the transactionId is unknown
   */
  public TransactionResult getTransactionResult(String transactionId) {
    if (transactions.containsKey(transactionId)) {
      LOG.info("Transaction '{}' found, returning result", transactionId);
      LocalDateTime result = transactions.get(transactionId);
      if (isComplete(result)) {
        LOG.info("Transaction complete");
        return new TransactionResult(true, "A");
      } else {
        LOG.info("Transaction running");
        return new TransactionResult(false, null);
      }
    } else {
      LOG.error("Transaction '{}' not present", transactionId);
      throw new IllegalStateException(String.format("Transaction '%s' not present", transactionId));
    }
  }

  private boolean isComplete(LocalDateTime completionTime) {
    return completionTime.isBefore(LocalDateTime.now());
  }

  /**
   * Completes the transaction by releasing the given transactionId
   *
   * @param transactionId the ID of the transaction to be completed
   * @throws IllegalStateException if the transaction to be complete has no result yet
   */
  public void completeTransaction(String transactionId) {
    if (transactions.containsKey(transactionId)) {
      LocalDateTime result = transactions.get(transactionId);
      if (isComplete(result)) {
        transactions.remove(transactionId);
        LOG.info("Completed transaction '{}'", transactionId);
      } else {
        LOG.info("Unable to complete transaction '{}'", transactionId);
        throw new IllegalStateException(
            String.format("Unable to complete transaction '%s'", transactionId));
      }
    }
  }

  /**
   * A transaction result
   *
   * @param complete indicates whether the transaction is complete
   * @param result the result. Only set if complete is true
   */
  public record TransactionResult(boolean complete, String result) {}
}
