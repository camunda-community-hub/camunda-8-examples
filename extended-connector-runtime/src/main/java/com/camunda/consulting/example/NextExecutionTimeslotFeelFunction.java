package com.camunda.consulting.example;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Function;
import org.camunda.feel.syntaxtree.Val;
import org.camunda.feel.syntaxtree.ValDayTimeDuration;
import org.springframework.stereotype.Component;

@Component
public class NextExecutionTimeslotFeelFunction implements Function<List<Val>, Val> {
  public static final List<String> PARAMS = List.of("backoff");
  public static final String NAME = "nextExecutionBackoff";

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
    if (scheduledExecution instanceof ValDayTimeDuration duration) {
      Duration value = duration.value();
      return new ValDayTimeDuration(calculateNextExecution(value));
    } else {
      throw new IllegalStateException(
          "Param 'scheduledExecution' expected to be of type 'date and time'");
    }
  }

  private Duration calculateNextExecution(Duration duration) {
    // we use the same "now" for both transformations to prevent inconsistencies
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime scheduledExecution = now.plus(duration);
    return Duration.between(now, schedulingService.schedule(scheduledExecution))
        .truncatedTo(ChronoUnit.SECONDS);
  }
}
