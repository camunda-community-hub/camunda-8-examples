package com.camunda.consulting.eventprocessing;

import static org.assertj.core.api.Assertions.*;

import com.camunda.consulting.eventprocessing.EventController.CreateEventRequest;
import com.camunda.consulting.eventprocessing.EventController.CreateEventResponse;
import com.camunda.consulting.eventprocessing.EventService.Event.State.StateName;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.zeebe.client.ZeebeClient;
import java.io.IOException;
import java.io.InputStream;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@CamundaSpringProcessTest
public class ProcessTest {
  @Autowired ZeebeClient zeebeClient;

  @Autowired EventService eventService;

  @Autowired EventController eventController;

  private static CreateEventRequest loadCreateEventRequest() {
    try (InputStream stream =
        ProcessTest.class.getClassLoader().getResourceAsStream("createEventRequest.json")) {

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
    CreateEventResponse event = eventController.createEvent(loadCreateEventRequest()).getBody();
    // then
    Awaitility.await()
        .untilAsserted(
            () ->
                assertThat(eventService.getEvent(event.id()))
                    .isPresent()
                    .get()
                    .matches(e -> e.state().name().equals(StateName.PUBLISHED)));
  }
}
