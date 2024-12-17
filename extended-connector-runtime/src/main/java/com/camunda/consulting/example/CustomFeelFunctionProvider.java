package com.camunda.consulting.example;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.camunda.feel.context.JavaFunction;
import org.camunda.feel.context.JavaFunctionProvider;

public class CustomFeelFunctionProvider extends JavaFunctionProvider {

  @Override
  public Optional<JavaFunction> resolveFunction(String functionName) {
    if (NextExecutionTimeslotFeelFunction.NAME.equals(functionName)) {
      return Optional.of(
          new JavaFunction(
              NextExecutionTimeslotFeelFunction.PARAMS,
              NextExecutionTimeslotFeelFunction.getInstance()));
    }
    return Optional.empty();
  }

  @Override
  public Collection<String> getFunctionNames() {
    return List.of(NextExecutionTimeslotFeelFunction.NAME);
  }
}
