package com.camunda.consulting.eventprocessing;

import com.camunda.consulting.eventprocessing.EventRepository.EventEntity;
import com.camunda.consulting.eventprocessing.EventRepository.EventEntity.State;
import com.camunda.consulting.eventprocessing.EventService.Event.State.StateName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.camunda.consulting.eventprocessing.Mapper.*;

@Service
public class EventService {
  private final EventRepository eventRepository;

  public EventService(EventRepository eventRepository) {this.eventRepository = eventRepository;}

  public Event saveEvent(UnsavedEvent event) {
    return map(eventRepository
        .findById(event.id())
        .orElseGet(() -> eventRepository.save(map(event))));
  }

  public Page<Event> getEvents(Pageable pageable) {
    return eventRepository
        .findAll(pageable)
        .map(Mapper::map);
  }

  public Page<Event> getEventsWithState(Pageable pageable, StateName state) {
    return eventRepository
        .findAll(Example.of(entity(state)),pageable)
        .map(Mapper::map);
  }

  private static EventEntity entity(StateName state) {
    EventEntity entity = new EventEntity();
    entity.setState(map(state));
    return entity;
  }

  public Event updateEventStateToPublishing(String id) {
    EventEntity eventEntity = eventRepository
        .findById(id)
        .orElseThrow();
      eventEntity.setPublishingAt(LocalDateTime.now());

    eventEntity.setState(State.PUBLISHING);
    eventRepository.save(eventEntity);
    return map(eventEntity);
  }

  public Event updateEventStateToPublished(String id) {
    EventEntity eventEntity = eventRepository
        .findById(id)
        .orElseThrow();
      eventEntity.setPublishedAt(LocalDateTime.now());

    eventEntity.setState(State.PUBLISHED);
    eventRepository.save(eventEntity);
    return map(eventEntity);
  }

  public record UnsavedEvent(String id, String name, ObjectNode content) {}

  public record Event(String id, String name, ObjectNode content, State state) {
    public record State(StateName name, LocalDateTime createdAt, LocalDateTime publishingAt,
                        LocalDateTime publishedAt) {
      public enum StateName {CREATED, PUBLISHING, PUBLISHED}
    }
  }


}
