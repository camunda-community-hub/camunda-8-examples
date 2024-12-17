package com.camunda.consulting.example;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Function;
import org.camunda.feel.syntaxtree.Val;
import org.camunda.feel.syntaxtree.ValDateTime;
import org.springframework.stereotype.Component;

@Component
public class NextExecutionTimeslotFeelFunction implements Function<List<Val>, Val> {
  public static final List<String> PARAMS = List.of("scheduledExecution");
  public static final String NAME = "nextExecutionTimeslot";

  /**
   * This field is required to access a bean from outside the spring context as spi is used for the
   * function provider
   */
  private static NextExecutionTimeslotFeelFunction instance;

  private final SchedulingService schedulingService;

  public NextExecutionTimeslotFeelFunction(SchedulingService schedulingService) {
    this.schedulingService = schedulingService;
    instance = this;
  }

  public static NextExecutionTimeslotFeelFunction getInstance() {
    return instance;
  }

  @Override
  public Val apply(List<Val> vals) {
    Val scheduledExecution = vals.get(0);
    if (scheduledExecution instanceof ValDateTime valDateTime) {
      ZonedDateTime value = valDateTime.value();
      return new ValDateTime(calculateNextExecution(value));
    } else {
      throw new IllegalStateException(
          "Param 'scheduledExecution' expected to be of type 'date and time'");
    }
  }

  private ZonedDateTime calculateNextExecution(ZonedDateTime scheduledExecution) {
    return schedulingService.schedule(scheduledExecution);
  }
}
