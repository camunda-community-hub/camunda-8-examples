package com.camunda.consulting.eventprocessing;

import static com.camunda.consulting.eventprocessing.Mapper.*;

import com.camunda.consulting.eventprocessing.EventRepository.EventEntity;
import com.camunda.consulting.eventprocessing.EventRepository.EventEntity.State;
import com.camunda.consulting.eventprocessing.EventService.Event.State.StateName;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EventService {
  private final EventRepository eventRepository;

  public EventService(EventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  public Event saveEvent(UnsavedEvent event) {
    return map(
        eventRepository.findById(event.id()).orElseGet(() -> eventRepository.save(map(event))));
  }

  public Page<Event> getEvents(Pageable pageable) {
    return eventRepository.findAll(pageable).map(Mapper::map);
  }

  public Page<Event> getEventsWithState(Pageable pageable, StateName state) {
    return eventRepository.findAll(Example.of(entity(state)), pageable).map(Mapper::map);
  }

  public Optional<Event> getEvent(String id) {
    return eventRepository.findById(id).map(Mapper::map);
  }

  private static EventEntity entity(StateName state) {
    EventEntity entity = new EventEntity();
    entity.setState(map(state));
    return entity;
  }

  public Event updateEventStateToPublishing(String id) {
    EventEntity eventEntity = eventRepository.findById(id).orElseThrow();
    eventEntity.setPublishingAt(OffsetDateTime.now());

    eventEntity.setState(State.PUBLISHING);
    eventRepository.save(eventEntity);
    return map(eventEntity);
  }

  public Event updateEventStateToPublished(String id) {
    EventEntity eventEntity = eventRepository.findById(id).orElseThrow();
    eventEntity.setPublishedAt(OffsetDateTime.now());

    eventEntity.setState(State.PUBLISHED);
    eventRepository.save(eventEntity);
    return map(eventEntity);
  }

  public record UnsavedEvent(String id, String name, ObjectNode content) {}

  public record Event(String id, String name, ObjectNode content, State state) {
    public record State(
        StateName name,
        OffsetDateTime createdAt,
        OffsetDateTime publishingAt,
        OffsetDateTime publishedAt) {
      public enum StateName {
        CREATED,
        PUBLISHING,
        PUBLISHED
      }
    }
  }
}
