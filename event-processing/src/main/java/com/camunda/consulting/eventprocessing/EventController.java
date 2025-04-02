package com.camunda.consulting.eventprocessing;

import static com.camunda.consulting.eventprocessing.Mapper.*;

import com.camunda.consulting.eventprocessing.EventService.Event;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
public class EventController {
  private final EventService eventService;

  public EventController(EventService eventService) {
    this.eventService = eventService;
  }

  @PostMapping
  public ResponseEntity<CreateEventResponse> createEvent(@RequestBody CreateEventRequest request) {
    return ResponseEntity.ok(map(eventService.saveEvent(map(request))));
  }

  @GetMapping
  public ResponseEntity<PagedModel<GetEventResponse>> getEvents(Pageable pageable) {
    return ResponseEntity.ok(new PagedModel<>(eventService.getEvents(pageable).map(Mapper::map)));
  }

  @GetMapping("/{eventId}")
  public ResponseEntity<?> getEvent(@PathVariable String eventId) {
    Optional<Event> event = eventService.getEvent(eventId);
    if (event.isPresent()) {
      GetEventResponse response = map(event.get());
      return ResponseEntity.ok(response);
    }
    return ResponseEntity.notFound().build();
  }

  public record CreateEventRequest(String id, String name, ObjectNode content) {}

  public record CreateEventResponse(String id) {}

  public record GetEventResponse(String id, String name, JsonNode content, State state) {
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
