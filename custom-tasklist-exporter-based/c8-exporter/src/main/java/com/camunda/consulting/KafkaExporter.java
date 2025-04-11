package com.camunda.consulting;

import com.camunda.consulting.impl.BpmnDiagram;
import com.camunda.consulting.impl.UserTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.exporter.api.Exporter;
import io.camunda.zeebe.exporter.api.context.Context;
import io.camunda.zeebe.exporter.api.context.Controller;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.camunda.zeebe.protocol.record.intent.Intent;
import io.camunda.zeebe.protocol.record.intent.JobBatchIntent;
import io.camunda.zeebe.protocol.record.intent.JobIntent;
import io.camunda.zeebe.protocol.record.value.DeploymentRecordValue;
import io.camunda.zeebe.protocol.record.value.JobBatchRecordValue;
import io.camunda.zeebe.protocol.record.value.JobRecordValue;
import io.camunda.zeebe.protocol.record.value.deployment.DeploymentResource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaExporter implements Exporter {

  private String sourceId;
  private Producer<String, Object> producer;
  private static String exporterJobType = "io.camunda.zeebe:userTask";
  private static String kafkaTopicUsertask = "usertask-info";
  private static String kafkaTopicDeploy = "deploy-info";

  Logger logger = LoggerFactory.getLogger(KafkaExporter.class);

  @Override
  public void configure(Context context) throws Exception {
    Exporter.super.configure(context);
    Map config = context.getConfiguration().getArguments();
    this.sourceId = (String) config.get("sourceid");
    String kafkaUrl = (String) config.get("url");
    String jobType = (String) config.get("jobtype");
    String kafkaTopicUserTask = (String) config.get("kafkatopicusertask");
    String kafkaTopicDeploy = (String) config.get("kafkatopicdeploy");

    if (sourceId == null || kafkaUrl == null) {
      throw new IllegalArgumentException("sourceId and kafkaUrl are required");
    }

    if (jobType != null) {
      exporterJobType = jobType;
    }

    if (kafkaTopicUserTask != null) {
      kafkaTopicUsertask = kafkaTopicUserTask;
    }

    if (kafkaTopicDeploy != null) {
      KafkaExporter.kafkaTopicDeploy = kafkaTopicDeploy;
    }

    logger.info("Exporter Configuration: " + config);

    Properties props = new Properties();
    props.put("bootstrap.servers", kafkaUrl);
    props.put("key.serializer", StringSerializer.class.getName());
    props.put("value.serializer", JsonSerializer.class.getName());
    producer = new KafkaProducer<>(props);
  }

  @Override
  public void open(Controller controller) {
    Exporter.super.open(controller);
  }

  @Override
  public void close() {
    Exporter.super.close();
  }

  @Override
  public void export(Record<?> record) {

    RecordValue recordValue = record.getValue();
    Intent intent = record.getIntent();

    if (record.getRecordType() == RecordType.EVENT) {

      switch (record.getValueType()) {
        case JOB_BATCH:
          exportJobBatch((JobBatchRecordValue) recordValue, intent);
          break;
        case JOB:
          exportJob((JobRecordValue) recordValue, (JobIntent) intent);
          break;
        case DEPLOYMENT:
          exportDeployment((DeploymentRecordValue) recordValue, intent);
          break;
      }
    }
  }


  private void exportJobBatch(JobBatchRecordValue recordValue, Intent intent) {
    if (intent == JobBatchIntent.ACTIVATED) {
      logger.debug("Export JobBatchRecordValue: {}", recordValue);
      for (int i = 0; i < recordValue.getJobKeys().size(); i++) {
        long jobKey = recordValue.getJobKeys().get(i);
        JobRecordValue jobRecordValue = recordValue.getJobs().get(i);
        if(jobRecordValue.getType().equals(exporterJobType)) {
          UserTask userTask = toUserTask(sourceId, EventType.CREATED, jobRecordValue, jobKey);
          sendToKafka(recordValue.getJobKeys().get(i), userTask);
      }
      }
    }
  }

  private void exportJob(JobRecordValue jobRecordValue,
      JobIntent intent) {
    UserTask userTask;
    if (jobRecordValue.getType().equals(exporterJobType)) {
      switch (intent) {
        case CANCELED:
          userTask = toUserTask(sourceId, EventType.ENDED, jobRecordValue);
          logger.debug("UserTask canceled: {}", userTask);
          sendToKafka(jobRecordValue.getElementInstanceKey(), userTask);
          break;
        case COMPLETED:
          userTask = toUserTask(sourceId, EventType.COMPLETED, jobRecordValue);
          logger.debug("UserTask completed: {}", userTask);
          sendToKafka(jobRecordValue.getElementInstanceKey(), userTask);
          break;
      }
    }
  }

  private void exportDeployment(DeploymentRecordValue recordValue, Intent intent) {
    List<DeploymentResource> resources = recordValue.getResources();
    for (int i = 0; i < resources.size(); i++) {
      DeploymentResource resource = resources.get(i);
      String resourceName = resource.getResourceName();
      if(resourceName.endsWith(".bpmn")) {
        String id = String.valueOf(
            recordValue.getProcessesMetadata().get(i).getProcessDefinitionKey());
        String processDefinition = new String(resource.getResource());
        BpmnDiagram bpmnDiagram = new BpmnDiagram(id, processDefinition);
        ObjectMapper mapper = new ObjectMapper();

        ProducerRecord<String, Object> record = new ProducerRecord<String, Object>(kafkaTopicDeploy, id, mapper.valueToTree(bpmnDiagram));
        record.headers().add("__TypeId__", bpmnDiagram.getClass().getCanonicalName().getBytes());
        producer.send(record);
      }

    }
  }

  private void sendToKafka(long key, UserTask userTask) {

    try {
      ObjectMapper mapper = new ObjectMapper();
      ProducerRecord<String, Object> record = new ProducerRecord<>(kafkaTopicUsertask, String.valueOf(key),
          mapper.valueToTree(userTask));
      record.headers().add("__TypeId__", userTask.getClass().getCanonicalName().getBytes());
      producer.send(record);
    } catch (Exception ex) {
      logger.error("Error sending userTask to kafka: {}", ex.getMessage());
    }
  }

  private UserTask toUserTask(String sourceId, EventType eventType, JobRecordValue jobRecordValue) {
    String priorityString = jobRecordValue.getCustomHeaders().get("priority");
    int priority = priorityString == null ? 0 : Integer.parseInt(priorityString);

    UserTask task = UserTask.builder()
        .userTaskId(String.valueOf(jobRecordValue.getElementInstanceKey()))
        .source(sourceId)
        .eventType(eventType)
        .processInstanceId(String.valueOf(jobRecordValue.getProcessInstanceKey()))
        .processDefinitionId(String.valueOf(jobRecordValue.getProcessDefinitionKey()))
        .taskElementName(jobRecordValue.getElementId())
        .formKey(jobRecordValue.getCustomHeaders().get("io.camunda.zeebe:formKey"))
        .assignee(jobRecordValue.getCustomHeaders().get("io.camunda.zeebe:assignee"))
        .candidateGroups(candidateStringToList(
            jobRecordValue.getCustomHeaders().get("io.camunda.zeebe:candidateGroups")))
        .candidateUsers(
            candidateStringToList(jobRecordValue.getCustomHeaders().get("candidateUsers")))
        .dueDate(jobRecordValue.getCustomHeaders().get("dueDate"))
        .followUpDate(jobRecordValue.getCustomHeaders().get("followUpDate"))
        .priority(priority)
        .variables(jobRecordValue.getVariables())
        .build();

    return task;
  }

  private UserTask toUserTask(String sourceId, EventType eventType, JobRecordValue jobRecordValue,
      long jobKey) {
    UserTask task = toUserTask(sourceId, eventType, jobRecordValue);
    task.setJobKey(String.valueOf(jobKey));
    return task;
  }

  private List<String> candidateStringToList(String candidateString) {
    if (candidateString != null) {
      String reducedCandidateString = candidateString
          .replace("[", "")
          .replace("]", "")
          .replace(" ", "")
          .replace("\"", "");
      return Arrays.asList(reducedCandidateString.split(","));
    }
    return null;
  }

  void setProducer(Producer producer) {
    this.producer = producer;
  }

}
