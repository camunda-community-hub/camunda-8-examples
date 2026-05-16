package org.camunda.bpm.run;

import static org.junit.jupiter.api.Assertions.*;

import com.camunda.consulting.impl.CompletedTaskMessage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.test.junit5.ProcessEngineExtension;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EmbeddedKafka(partitions = 1 , brokerProperties = { "listeners=PLAINTEXT://localhost:9092" , "port=9092" })
@ExtendWith(SpringExtension.class)
public class ExporterTest {

  @RegisterExtension
  ProcessEngineExtension extension = ProcessEngineExtension.builder()
      .configurationResource("camunda.cfg.xml")
      .build();

  private static final String USERTASK_TOPIC = "usertask-info" ;
  private static final String DEPLOY_TOPIC = "deploy-info" ;
  private static final String ENGINE_TOPIC = "C7Engine" ;

  private static final String PROCESS_KEY = "test" ;
  private static final String PROCESS_FILE_NAME = "test.bpmn" ;

  private RuntimeService runtimeService;
  private RepositoryService repositoryService;
  private TaskService taskService;
  @Autowired
  private EmbeddedKafkaBroker embeddedKafkaBroker;

  @BeforeEach
  public void setup() {
    taskService = extension.getProcessEngine().getTaskService();
    runtimeService = extension.getProcessEngine().getRuntimeService();
    repositoryService = extension.getProcessEngine().getRepositoryService();
  }

  @Test
  public void whenDeploySendToTopic() {
    // Given
    Consumer consumer = configureConsumer(DEPLOY_TOPIC);
    BpmnModelInstance model = Bpmn.createExecutableProcess(PROCESS_KEY)
        .name(PROCESS_KEY)
        .startEvent()
        .endEvent()
        .done();
    // When
    repositoryService.createDeployment()
      .addModelInstance(PROCESS_FILE_NAME, model)
      .deploy();

    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
    assertEquals(1, query.count());
    String id = query.list().get(0).getId();
    // Then
    ConsumerRecord<Object, Object> record = KafkaTestUtils.getSingleRecord(consumer, DEPLOY_TOPIC);

    assertNotNull(record);
    assertEquals(id, record.key());
    assertTrue(record.value().toString().contains( "processDefinition"));
  }

  @Test
  public void whenCompleteUserTaskReceivedCompleteOnEngine() throws InterruptedException {
    // Given
    createUserTask();
    assertEquals(1, taskService.createTaskQuery().count());
    String taskId = taskService.createTaskQuery().list().get(0).getId();
    Producer producer = configureProducer();
    CompletedTaskMessage message = new CompletedTaskMessage();
    message.setId(taskId);
    // When
    ProducerRecord<Object, Object> record = new ProducerRecord<>(ENGINE_TOPIC, taskId, message);
    producer.send(record);
    Thread.sleep(1000);
    // Then
    assertEquals(0, taskService.createTaskQuery().count());

  }

  // TODO Figure out how to use Event Plugin in Spring xml config
  //@Test
  public void whenCreateUserTaskSendToTopic() {
    // Given
    Consumer consumer = configureConsumer(USERTASK_TOPIC);
    createUserTask();
    // When
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    assertEquals(1, taskService.createTaskQuery().count());
    // Then
    ConsumerRecord<Object, Object> record = KafkaTestUtils.getSingleRecord(consumer, USERTASK_TOPIC);
    assertNotNull(record);
  }

  private Consumer<String, String> configureConsumer(String topic) {
    Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<String, String>(consumerProps,  new StringDeserializer(), new StringDeserializer())
        .createConsumer();
    consumer.subscribe(Collections.singleton(topic));
    return consumer;
  }

  private Producer<String, Object> configureProducer() {
    Map<String, Object> producerProps = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
    return new DefaultKafkaProducerFactory<String, Object>(producerProps, new StringSerializer(), new JsonSerializer<>()).createProducer();
  }

  private void createUserTask() {
    BpmnModelInstance model = Bpmn.createExecutableProcess(PROCESS_KEY)
        .name(PROCESS_KEY)
        .startEvent()
        .userTask()
        .endEvent()
        .done();

    repositoryService
        .createDeployment()
        .addModelInstance(PROCESS_FILE_NAME, model)
        .deploy();

    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
  }

}
