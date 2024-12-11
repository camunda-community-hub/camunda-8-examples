package com.camunda.consulting.eventprocessing;

import com.camunda.consulting.eventprocessing.EventService.Event;
import com.camunda.consulting.eventprocessing.EventService.Event.State.StateName;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.ClientStatusException;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import io.grpc.Status.Code;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;


@Component
@EnableScheduling
public class EventProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(EventProcessor.class);
  private final EventService eventService;
  private final ZeebeClient zeebeClient;

  public EventProcessor(EventService eventService, ZeebeClient zeebeClient) {
    this.eventService = eventService;
    this.zeebeClient = zeebeClient;
  }

  @Scheduled(fixedDelay = 100L)
  public void processCreatedEvents() {
    eventService
        .getEventsWithState(Pageable.ofSize(100), StateName.CREATED)
        .forEach(this::processEvent);
  }

  @Scheduled(fixedDelay = 60000L)
  public void processPublishingEvents() {
    eventService
        .getEventsWithState(Pageable.ofSize(100), StateName.PUBLISHING)
        .filter(e -> e
            .state()
            .publishingAt() == null || e
            .state()
            .publishingAt()
            .isBefore(LocalDateTime
                .now()
                .minus(Duration.ofMinutes(10))))
        .forEach(this::processEvent);
  }

  void processEvent(Event event) {
    event = eventService.updateEventStateToPublishing(event.id());
    LOG.info("Processing event: {}", event);
    try {
      zeebeClient
          .newPublishMessageCommand()
          .messageName(event.name())
          .correlationKey(event.id())
          .messageId(event.id())
          .variables(Map.of("eventId", event.id(), "content", event.content()))
          .timeToLive(Duration.ofMinutes(10))
          .send()
          .join();
    } catch (ClientStatusException exception){
      if(!exception.getStatusCode().equals(Code.ALREADY_EXISTS)){
        throw exception;
      } else {
        LOG.warn("Event {} already published", event.name());
      }
    }
  }



  @JobWorker(type = "correlated")
  public void handleCorrelatedEvent(@Variable String eventId){
    Event event = eventService.updateEventStateToPublished(eventId);
    LOG.info("Event is published: {}", event);
  }
}
