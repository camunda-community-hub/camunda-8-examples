import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1.PublishMessageCommandStep3;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.builder.ProcessBuilder;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.MessageAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.*;

@ZeebeProcessTest
public class MessageCorrelationTest {

  ZeebeClient zeebeClient;
  ZeebeTestEngine zeebeTestEngine;

  @TestFactory
  Stream<DynamicTest> shouldRunProcess() {
    // name is always the same
    // correlaction key can be set or not
    List<PropertyState> correlationKeys = Arrays.asList(PropertyState.values());
    // time to live can be set or be zero
    List<PropertyState> timeToLives = Arrays.asList(PropertyState.values());
    // message id can be set or not
    List<PropertyState> messageIds = Arrays.asList(PropertyState.values());
    // Start or intermediate
    List<EventType> eventTypes = Arrays.asList(EventType.values());
    return eventTypes
        .stream()
        .flatMap(eventType -> correlationKeys
            .stream()
            .flatMap(correlationKey -> timeToLives
                .stream()
                .flatMap(timeToLive -> messageIds
                    .stream()
                    .filter(p -> !(eventType.equals(EventType.Intermediate) && correlationKey.equals(PropertyState.notSet)))
                    .map(messageId -> DynamicTest.dynamicTest(displayname(eventType,
                            correlationKey,
                            timeToLive,
                            messageId
                        ),
                        () -> testMessageBehaviour(eventType, correlationKey, timeToLive, messageId)
                    )))));
  }

  private String displayname(
      EventType eventType, PropertyState correlationKey, PropertyState timeToLive, PropertyState messageId
  ) {
    return "Message(eventType: " + eventType + ", correlationKey " + correlationKey + ", timeToLive " + timeToLive + ", messageId " + messageId + ")";
  }

  private void testMessageBehaviour(
      EventType eventType, PropertyState correlationKey, PropertyState timeToLive, PropertyState messageId
  ) throws InterruptedException, TimeoutException {
    String name = RandomStringUtils.randomAlphabetic(20);
    String messageIdValue = UUID
        .randomUUID()
        .toString();
    String correlationKeyValue = correlationKey.equals(PropertyState.set) ? "correlationKey" : "";
    prepareProcess(eventType, name, correlationKeyValue);
    correlateMessage(name, correlationKeyValue, timeToLive, messageId, messageIdValue, eventType, 1);
    if (!messageId.equals(PropertyState.set)) {
      correlateMessage(name, correlationKeyValue, timeToLive, messageId, messageIdValue, eventType, 2);
    }
    zeebeTestEngine.increaseTime(Duration.ofSeconds(10));
    zeebeTestEngine.waitForBusyState(Duration.ofSeconds(10));
    zeebeTestEngine.waitForIdleState(Duration.ofSeconds(10));
    correlateMessage(name, correlationKeyValue, timeToLive, messageId, messageIdValue, eventType, 2);
  }

  @Test
  void shouldSimulate_EventType_Start_correlationKey_Set_ttl_Set_messageId_set() throws
      InterruptedException,
      TimeoutException {
    String name = RandomStringUtils.randomAlphabetic(20);
    String messageIdValue = UUID
        .randomUUID()
        .toString();
    String correlationKeyValue = "correlationKey";
    prepareProcess(EventType.Start, name, correlationKeyValue);
    correlateMessage(name,
        correlationKeyValue,
        PropertyState.set,
        PropertyState.set,
        messageIdValue,
        EventType.Start,
        1
    );
    zeebeTestEngine.increaseTime(Duration.ofSeconds(10));
    zeebeTestEngine.waitForBusyState(Duration.ofSeconds(10));
    zeebeTestEngine.waitForIdleState(Duration.ofSeconds(10));
    correlateMessage(name,
        correlationKeyValue,
        PropertyState.set,
        PropertyState.set,
        messageIdValue,
        EventType.Start,
        1
    );
  }

  private void correlateMessage(
      String name,
      String correlationKeyValue,
      PropertyState timeToLive,
      PropertyState messageId,
      String messageIdValue,
      EventType eventType,
      int invocationCounter
  ) {
    PublishMessageCommandStep3 builder = zeebeClient
        .newPublishMessageCommand()
        .messageName(name)
        .correlationKey(correlationKeyValue);
    if (timeToLive.equals(PropertyState.set)) {
      builder = builder.timeToLive(Duration.ofMinutes(1));
    } else {
      builder = builder.timeToLive(Duration.ZERO);
    }
    if (messageId.equals(PropertyState.set)) {
      builder = builder.messageId(messageIdValue);
    }
    PublishMessageResponse messageResponse = builder
        .send()
        .join();
    MessageAssert messageAssert = assertThat(messageResponse);
    if (eventType.equals(EventType.Start)) {
      messageAssert.hasCreatedProcessInstance();
    } else {
      messageAssert.hasBeenCorrelated();
    }
  }

  private void prepareProcess(EventType eventType, String name, String correlationKey) {
    BpmnModelInstance bpmnModelInstance = build(name, eventType, name, correlationKey);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Bpmn.writeModelToStream(out, bpmnModelInstance);
    System.out.println(new String(out.toByteArray()));
    zeebeClient
        .newDeployResourceCommand()
        .addProcessModel(bpmnModelInstance, name + ".bpmn")
        .send()
        .join();
    if (eventType.equals(EventType.Intermediate)) {
      zeebeClient
          .newCreateInstanceCommand()
          .bpmnProcessId(name)
          .latestVersion()
          .send()
          .join();
      zeebeClient
          .newCreateInstanceCommand()
          .bpmnProcessId(name)
          .latestVersion()
          .send()
          .join();
    }
  }

  private BpmnModelInstance build(String processId, EventType eventType, String name, String correlationKey) {
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess(processId);
    if (eventType.equals(EventType.Start)) {
      return processBuilder
          .startEvent()
          .message(builder -> builder.name(name))
          .intermediateCatchEvent()
          .timerWithDuration(Duration.ofSeconds(10))
          .endEvent()
          .done();
    }
    if (eventType.equals(EventType.Intermediate)) {
      return processBuilder
          .startEvent()
          .intermediateCatchEvent()
          .message(builder -> builder
              .name(name)
              .zeebeCorrelationKeyExpression("=\"" + correlationKey + "\""))
          .intermediateCatchEvent()
          .timerWithDuration(Duration.ofSeconds(10))
          .endEvent()
          .done();
    }
    throw new IllegalStateException("Unhandled EventType " + eventType);
  }

  public enum EventType {Start, Intermediate}

  public enum PropertyState {set, notSet}
}


