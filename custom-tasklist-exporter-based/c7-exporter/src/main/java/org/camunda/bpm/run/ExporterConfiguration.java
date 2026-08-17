package org.camunda.bpm.run;


import lombok.Data;

@Data
public class ExporterConfiguration {

  protected String bootstrapServer = "";
  protected String userTaskTopic = "usertask-info";
  protected String deployTopic = "deploy-info";
  protected String sourceId = "";
  protected String clientId = "";
  protected String groupId = "";
  protected String producer_value_serializer = "org.springframework.kafka.support.serializer.JsonSerializer";
  protected String producer_key_serializer = "org.apache.kafka.common.serialization.StringSerializer";
  protected String consumer_value_deserializer = "org.springframework.kafka.support.serializer.JsonDeserializer";
  protected String consumer_key_deserializer = "org.apache.kafka.common.serialization.StringDeserializer";
  protected boolean useTypeHeaders = false;
  protected String defaultType = "com.camunda.consulting.impl.CompletedTaskMessage";

}
