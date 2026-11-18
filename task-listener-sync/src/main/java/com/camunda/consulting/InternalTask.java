package com.camunda.consulting;

import java.util.Map;
import java.util.function.Supplier;

public record InternalTask(
    Map<String, Object> variables, Supplier<Object> formSupplier, State state, SyncType syncType) {
  public enum State {
    CREATED,
    COMPLETED,
    CANCELED
  }

  public enum SyncType {
    REACTIVE,
    POLLING
  }
}
