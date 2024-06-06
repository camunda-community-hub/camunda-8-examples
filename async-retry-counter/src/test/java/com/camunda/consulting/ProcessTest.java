package com.camunda.consulting;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.filters.RecordStream;
import io.camunda.zeebe.process.test.filters.StreamFilter;
import io.camunda.zeebe.process.test.inspections.model.InspectedProcessInstance;
import io.camunda.zeebe.protocol.record.intent.JobIntent;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.*;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.*;

@ZeebeSpringTest
@SpringBootTest
public class ProcessTest {
  @Autowired
  ZeebeClient zeebeClient;
  @Autowired
  ZeebeTestEngine zeebeTestEngine;

  @Test
  void shouldRetryThreeTimes() throws InterruptedException, TimeoutException {
    ProcessInstanceEvent process = zeebeClient
        .newCreateInstanceCommand()
        .bpmnProcessId("AsyncRetryCounterProcess")
        .latestVersion()
        .variable("callbackId", "123")
        .send()
        .join();
    waitForProcessInstanceHasPassedElement(process, "SendMessageTask");
    assertThat(process).hasVariableWithValue("retryCounter", 2);
    zeebeClient
        .newPublishMessageCommand()
        .messageName("fail")
        .correlationKey("123")
        .send()
        .join();
    zeebeTestEngine.waitForIdleState(Duration.ofSeconds(10));
    zeebeTestEngine.increaseTime(Duration.ofSeconds(5));
    waitForProcessInstanceHasPassedElement(new InspectedProcessInstance(process.getProcessInstanceKey()),
        "SendMessageTask",
        Duration.ofSeconds(10),
        2
    );
    assertThat(process).hasVariableWithValue("retryCounter", 1);
    zeebeClient
        .newPublishMessageCommand()
        .messageName("fail")
        .correlationKey("123")
        .send()
        .join();
    zeebeTestEngine.waitForIdleState(Duration.ofSeconds(10));
    zeebeTestEngine.increaseTime(Duration.ofSeconds(5));
    waitForProcessInstanceHasPassedElement(new InspectedProcessInstance(process.getProcessInstanceKey()),
        "SendMessageTask",
        Duration.ofSeconds(10),
        3
    );
    assertThat(process).hasVariableWithValue("retryCounter", 0);
    zeebeClient
        .newPublishMessageCommand()
        .messageName("fail")
        .correlationKey("123")
        .send()
        .join();
    zeebeTestEngine.waitForIdleState(Duration.ofSeconds(10));
    zeebeTestEngine.increaseTime(Duration.ofSeconds(5));
    waitForProcessInstanceHasPassedElement(new InspectedProcessInstance(process.getProcessInstanceKey()),
        "Gateway_1uhx8ty",
        Duration.ofSeconds(10),
        4
    );
    zeebeTestEngine.waitForIdleState(Duration.ofSeconds(10));
    long incidentKey = assertThat(process)
        .hasAnyIncidents()
        .extractingLatestIncident()
        .getIncidentKey();
    long jobKey = StreamFilter
        .jobRecords(RecordStream.of(zeebeTestEngine.getRecordStreamSource()))
        .withIntent(JobIntent.FAILED)
        .stream()
        .findFirst()
        .get()
        .getKey();
    zeebeClient
        .newUpdateRetriesCommand(jobKey)
        .retries(1)
        .send()
        .join();
    zeebeClient
        .newResolveIncidentCommand(incidentKey)
        .send()
        .join();
    zeebeClient
        .newPublishMessageCommand()
        .messageName("success")
        .correlationKey("123")
        .send()
        .join();
    waitForProcessInstanceCompleted(process);
  }
}
