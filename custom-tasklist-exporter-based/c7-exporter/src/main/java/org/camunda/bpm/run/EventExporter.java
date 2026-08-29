package org.camunda.bpm.run;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer;
import org.camunda.bpm.run.consumer.ActivityHandler;
import org.camunda.bpm.run.listener.BpmnDeployListener;
import org.camunda.bpm.run.producer.UserTaskProducer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class EventExporter extends ExporterConfiguration implements ProcessEnginePlugin {

  private static KafkaConsumer consumer;

  private static UserTaskProducer userTaskProducer;


  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    List<Deployer> customPostDeployers = processEngineConfiguration.getCustomPostDeployers();

    if (customPostDeployers == null) {
      customPostDeployers = new ArrayList<>();
    }

    customPostDeployers.add(new BpmnDeployListener());
    processEngineConfiguration.setCustomPostDeployers(customPostDeployers);

    createConsumer();
    createProducer();
  }

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {

  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
    TaskService taskService = processEngine.getTaskService();
    ActivityHandler activityHandler = new ActivityHandler(taskService, consumer, this.sourceId);
    Thread thread = new Thread(activityHandler);
    thread.start();

  }

  private void createConsumer() {
    Properties config = new Properties();
    config.put("bootstrap.servers", bootstrapServer);
    config.put("client.id", clientId);
    config.put("group.id", groupId);
    config.put("key.deserializer", consumer_key_deserializer);
    config.put("value.deserializer", consumer_value_deserializer);
    config.put("spring.json.use.type.headers", useTypeHeaders);
    config.put("spring.json.value.default.type", defaultType);
    consumer = new KafkaConsumer<String, Object>(config);
  }

  private void createProducer() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, producer_key_serializer);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, producer_value_serializer);
    KafkaTemplate template = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    userTaskProducer = new UserTaskProducer(sourceId, userTaskTopic, deployTopic, template);
  }

  public static UserTaskProducer getUserTaskProducer() {
    return userTaskProducer;
  }

  public static KafkaConsumer getConsumer() {
    return consumer;
  }

}
