package com.camunda.consulting.example;

import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
public class SchedulingService {
  public ZonedDateTime schedule(ZonedDateTime scheduledExecution) {
    // you can call your scheduling system here
    return scheduledExecution.plusMinutes(10);
  }
}
