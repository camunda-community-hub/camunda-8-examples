package com.camunda.consulting.eventprocessing;

import com.camunda.consulting.eventprocessing.EventController.CreateEventRequest;
import com.camunda.consulting.eventprocessing.EventController.CreateEventResponse;
import com.camunda.consulting.eventprocessing.EventService.Event;
import com.camunda.consulting.eventprocessing.EventService.Event.State.StateName;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.zeebe.client.ZeebeClient;
import org.assertj.core.api.Condition;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@CamundaSpringProcessTest
public class ProcessTest {
  @Autowired
  ZeebeClient zeebeClient;

  @Autowired
  EventService eventService;

  @Autowired
  EventController eventController;

  private static CreateEventRequest loadCreateEventRequest() {
    try (
        InputStream stream = ProcessTest.class
            .getClassLoader()
            .getResourceAsStream("createEventRequest.json")
    ) {

      return new ObjectMapper().readValue(stream, CreateEventRequest.class);
    } catch (IOException e) {
      throw new RuntimeException("Error while loading unsaved event", e);
    }
  }

  @BeforeEach
  void deployProcess() {
    zeebeClient
        .newDeployResourceCommand()
        .addResourceFromClasspath("eventHandler.bpmn")
        .send()
        .join();
  }

  @Test
  void shouldPublishEvent() {
    // when
    CreateEventResponse event = eventController
        .createEvent(loadCreateEventRequest())
        .getBody();
    Condition<Event> condition = new Condition<>(
        e -> e
            .id()
            .equals(event.id()), "Should be same event id"
    );
    // then
    Awaitility
        .await()
        .untilAsserted(() -> assertThat(eventService.getEventsWithState(
            Pageable.unpaged(),
            StateName.PUBLISHED
        )).haveExactly(1, condition));
  }
}
