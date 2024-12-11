package com.camunda.consulting.eventprocessing;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static com.camunda.consulting.eventprocessing.Mapper.*;

@RestController
@RequestMapping("/event")
public class EventController {
  private final EventService eventService;

  public EventController(EventService eventService) {
    this.eventService = eventService;
  }

  @PostMapping
  public ResponseEntity<CreateEventResponse> createEvent(@RequestBody CreateEventRequest request){
    return ResponseEntity.ok(map(eventService.saveEvent(map(request))));
  }

  @GetMapping
  public ResponseEntity<PagedModel<GetEventResponse>> getEvents(Pageable pageable){
    return ResponseEntity.ok(new PagedModel<>(eventService.getEvents(pageable).map(Mapper::map)));
  }

  public record CreateEventRequest(@NotNull String id, String name, ObjectNode content){}
  public record CreateEventResponse(String id){}
  public record GetEventResponse(String id, String name, JsonNode content, State state){
    public record State(StateName name, LocalDateTime createdAt, LocalDateTime publishingAt, LocalDateTime publishedAt) {
      public enum StateName {CREATED, PUBLISHING, PUBLISHED}
    }
  }
}
