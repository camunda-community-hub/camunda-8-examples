package com.camunda.consulting.eventprocessing;

import com.camunda.consulting.eventprocessing.EventController.CreateEventRequest;
import com.camunda.consulting.eventprocessing.EventController.CreateEventResponse;
import com.camunda.consulting.eventprocessing.EventController.GetEventResponse;
import com.camunda.consulting.eventprocessing.EventRepository.EventEntity;
import com.camunda.consulting.eventprocessing.EventRepository.EventEntity.State;
import com.camunda.consulting.eventprocessing.EventService.Event;
import com.camunda.consulting.eventprocessing.EventService.UnsavedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class Mapper {
  private static final Map<Class<?>, Map<Class<?>, Function<?, ?>>> MAPPER_REGISTRY = new HashMap<>();
  private static ObjectMapper objectMapper = new ObjectMapper();

  static {
    Arrays
        .stream(Mapper.class.getDeclaredMethods())
        .filter(m -> Modifier.isStatic(m.getModifiers()))
        .filter(m -> Modifier.isPrivate(m.getModifiers()))
        .filter(m -> Objects.nonNull(m.getAnnotation(IsMapper.class)))
        .forEach(Mapper::process);
  }

  private static void process(Method method) {
    // only 1 parameter allowed
    if (method.getParameterCount() != 1) {
      throw new RuntimeException("Method must have exactly one parameter");
    }
    Class<?> sourceType = method.getParameterTypes()[0];
    Class<?> targetType = method.getReturnType();
    Map<Class<?>, Function<?, ?>> targetMap = MAPPER_REGISTRY.computeIfAbsent(sourceType, (c) -> new HashMap<>());
    if (targetMap.containsKey(targetType)) {
      throw new RuntimeException("There are 2 functions defined for mapping " + sourceType + " to " + targetType);
    }
    targetMap.put(
        targetType, (s) -> {
          try {
            return method.invoke(null, s);
          } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Error while invoking mapping method", e);
          }
        }
    );
  }

  @SafeVarargs
  public static <S, T> T map(S event, T... omit) {
    Class<?> sourceType = event.getClass();
    Class<?> targetType = omit
        .getClass()
        .getComponentType();
    if (MAPPER_REGISTRY.containsKey(sourceType)) {
      Map<Class<?>, Function<?, ?>> targetMap = MAPPER_REGISTRY.get(sourceType);
      if (targetMap.containsKey(targetType)) {
        Function<S, T> function = (Function<S, T>) targetMap.get(targetType);
        return function.apply(event);
      } else {
        throw new RuntimeException("There are no functions defined for mapping " + sourceType + " to " + targetType);
      }
    } else {
      throw new RuntimeException("There are no functions defined for mapping " + sourceType + " to " + targetType);
    }
  }

  @IsMapper

  private static EventEntity mapToEventEntity(UnsavedEvent event) {
    EventEntity eventEntity = new EventEntity();
    eventEntity.setId(event.id());
    eventEntity.setName(event.name());
    eventEntity.setContent(mapToString(event.content()));
    eventEntity.setState(State.CREATED);
    eventEntity.setCreatedAt(LocalDateTime.now());
    return eventEntity;
  }

  @IsMapper

  private static String mapToString(JsonNode node) {
    try {
      return objectMapper.writeValueAsString(node);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error while writing json node to string", e);
    }
  }

  @IsMapper

  private static ObjectNode mapToJsonNode(String json) {
    try {
      return (ObjectNode) objectMapper.readTree(json);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error while reading string to json node", e);
    }
  }

  @IsMapper

  private static Event mapToEvent(EventEntity eventEntity) {
    return new Event(
        eventEntity.getId(),
        eventEntity.getName(),
        mapToJsonNode(eventEntity.getContent()),
        mapToState(eventEntity)
    );
  }

  @IsMapper

  private static Event.State mapToState(EventEntity eventEntity) {
    return new Event.State(
        mapToStateName(eventEntity.getState()),
        eventEntity.getCreatedAt(),
        eventEntity.getPublishingAt(),
        eventEntity.getPublishedAt()
    );
  }

  @IsMapper

  private static Event.State.StateName mapToStateName(EventEntity.State state) {
    return switch (state) {
      case CREATED -> Event.State.StateName.CREATED;
      case PUBLISHED -> Event.State.StateName.PUBLISHED;
      case PUBLISHING -> Event.State.StateName.PUBLISHING;
      case null -> null;
    };
  }

  @IsMapper
  private static EventEntity.State mapToState(Event.State.StateName stateName){
    return switch (stateName) {
      case CREATED -> EventEntity.State.CREATED;
      case PUBLISHED -> EventEntity.State.PUBLISHED;
      case PUBLISHING -> EventEntity.State.PUBLISHING;
      case null -> null;
    };
  }

  @IsMapper

  private static UnsavedEvent mapToUnsavedEvent(CreateEventRequest event) {
    return new UnsavedEvent(event.id(),event.name(), event.content());
  }

  @IsMapper

  private static CreateEventResponse mapToCreatedEventResponse(Event event) {
    return new CreateEventResponse(event.id());
  }

  @IsMapper

  private static GetEventResponse mapToGetEventResponse(Event event) {
    return new GetEventResponse(event.id(), event.name(), event.content(), mapToState(event.state()));
  }

  @IsMapper

  private static GetEventResponse.State mapToState(Event.State event) {
    return new GetEventResponse.State(
        mapToStateName(event.name()),
        event.createdAt(),
        event.publishingAt(),
        event.publishedAt()
    );
  }

  @IsMapper
  private static GetEventResponse.State.StateName mapToStateName(Event.State.StateName state) {
    return switch (state) {
      case CREATED -> GetEventResponse.State.StateName.CREATED;
      case PUBLISHED -> GetEventResponse.State.StateName.PUBLISHED;
      case PUBLISHING -> GetEventResponse.State.StateName.PUBLISHING;
      case null -> null;
    };
  }

  @Retention(RetentionPolicy.RUNTIME)
  private @interface IsMapper {}
}
