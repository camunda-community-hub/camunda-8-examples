package com.camunda.consulting.example;

import java.time.ZonedDateTime;
import org.springframework.stereotype.Service;

@Service
public class SchedulingService {
  public ZonedDateTime schedule(ZonedDateTime scheduledExecution) {
    // you can call your scheduling system here
    return scheduledExecution.plusMinutes(10);
  }
}
