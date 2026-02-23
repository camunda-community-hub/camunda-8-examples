package com.camunda.consulting;

import static org.junit.jupiter.api.Assertions.*;

import com.camunda.consulting.impl.BpmnDiagram;
import com.camunda.consulting.impl.UserTask;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.protocol.record.ImmutableRecord;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.JobBatchIntent;
import io.camunda.zeebe.protocol.record.intent.JobIntent;
import io.camunda.zeebe.protocol.record.value.ImmutableDeploymentRecordValue;
import io.camunda.zeebe.protocol.record.value.ImmutableJobBatchRecordValue;
import io.camunda.zeebe.protocol.record.value.ImmutableJobRecordValue;
import io.camunda.zeebe.protocol.record.value.deployment.ImmutableDeploymentResource;
import io.camunda.zeebe.protocol.record.value.deployment.ImmutableProcessMetadataValue;
import java.util.List;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class ExporterTest {

  private final KafkaExporter exporter = new KafkaExporter();
  private final MockProducer<String, JsonNode> mockProducer = new MockProducer<>(true, new StringSerializer(), new JsonSerializer());

  private final ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  public void setup() {
    exporter.setProducer(mockProducer);
  }

  @Test
  public void whenDeploySendToTopic() {
    // Given
    String proccesId = "aProcessId";
    Long processKey = 123L;
    String resourceName = "aResourceName.bpmn";
    String bpmnXml = "aBpmnXml";

    ImmutableDeploymentResource resource = ImmutableDeploymentResource.builder()
        .withResourceName("test.bpmn")
        .withResource(bpmnXml.getBytes())
        .build();

    ImmutableRecord record = ImmutableRecord.builder().withRecordType(RecordType.EVENT)
        .withValueType(ValueType.DEPLOYMENT)
        .withValue(ImmutableDeploymentRecordValue.builder()
            .withResources(List.of(resource))
            .withProcessesMetadata(List.of(ImmutableProcessMetadataValue.builder()
                .withResourceName(resourceName)
                .withProcessDefinitionKey(processKey)
                .withBpmnProcessId(proccesId)
                .build()))
            .build())
        .build();

    // When
    exporter.export(record);
    BpmnDiagram diagram = mapper.convertValue(mockProducer.history().get(0).value(), BpmnDiagram.class);

    // Then
    assertEquals(1, mockProducer.history().size());
    assertEquals(processKey.toString(), mockProducer.history().get(0).key());
    assertEquals(processKey.toString(), diagram.getDiagramId());
    assertEquals(bpmnXml, diagram.getProcessDefinition());
  }

  @Test
  public void whenUserTaskSendToTopic() {
    // Given
    Long jobKey = 123L;
    Long elementInstanceKey = 456L;
    Long processKey = 789L;

    ImmutableJobRecordValue jobRecordValue = getImmutableJobRecordValue(elementInstanceKey, processKey);

    ImmutableJobBatchRecordValue jobBatchRecordValue = ImmutableJobBatchRecordValue.builder()
        .withJobs(List.of(jobRecordValue))
        .withJobKeys(List.of(jobKey))
        .build();

    // When
    exporter.export(ImmutableRecord.builder().withRecordType(RecordType.EVENT)
        .withIntent(JobBatchIntent.ACTIVATED)
        .withValueType(ValueType.JOB_BATCH)
        .withValue(jobBatchRecordValue)
        .build());

    UserTask userTask = mapper.convertValue(mockProducer.history().get(0).value(), UserTask.class);

    // Then
    assertEquals(1, mockProducer.history().size());
    assertEquals(jobKey.toString(), mockProducer.history().get(0).key());
    assertEquals(jobKey.toString(), userTask.getJobKey());
    assertEquals(elementInstanceKey.toString(), userTask.getUserTaskId());
    assertEquals(processKey.toString(), userTask.getProcessDefinitionId());
    assertEquals(EventType.CREATED,userTask.getEventType());
  }

  @Test
  public void whenUserTaskCompleteSendToTopic() {
    // Given
    Long elementInstanceKey = 456L;
    Long processKey = 789L;

    ImmutableJobRecordValue jobRecordValue = getImmutableJobRecordValue(elementInstanceKey, processKey);

    exporter.export(ImmutableRecord.builder().withRecordType(RecordType.EVENT)
        .withIntent(JobIntent.COMPLETED)
        .withValueType(ValueType.JOB)
        .withValue(jobRecordValue)
        .build());

    assertEquals(1, mockProducer.history().size());
    UserTask userTask = mapper.convertValue(mockProducer.history().get(0).value(), UserTask.class);
    assertEquals(EventType.COMPLETED,userTask.getEventType());
  }

  @Test
  public void whenUserTaskCanceledSendToTopic() {
    // Given
    Long elementInstanceKey = 456L;
    Long processKey = 789L;

    ImmutableJobRecordValue jobRecordValue = getImmutableJobRecordValue(elementInstanceKey, processKey);

    // When
    exporter.export(ImmutableRecord.builder().withRecordType(RecordType.EVENT)
        .withIntent(JobIntent.CANCELED)
        .withValueType(ValueType.JOB)
        .withValue(jobRecordValue)
        .build());

    // Then
    assertEquals(1, mockProducer.history().size());
    UserTask userTask = mapper.convertValue(mockProducer.history().get(0).value(), UserTask.class);
    assertEquals(EventType.ENDED,userTask.getEventType());

  }

  private static ImmutableJobRecordValue getImmutableJobRecordValue(Long elementInstanceKey,
      Long processKey) {
    ImmutableJobRecordValue jobRecordValue = ImmutableJobRecordValue.builder()
        .withProcessDefinitionKey(processKey)
        .withProcessDefinitionVersion(1)
        .withType("io.camunda.zeebe:userTask")
        .withElementId("aElementId")
        .withElementInstanceKey(elementInstanceKey)
        .build();
    return jobRecordValue;
  }

}
