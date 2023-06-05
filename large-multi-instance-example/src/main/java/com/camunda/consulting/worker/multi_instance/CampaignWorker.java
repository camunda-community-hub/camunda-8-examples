package com.camunda.consulting.worker.multi_instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;

@Component
public class CampaignWorker {
  
  private static final Logger LOG = LoggerFactory.getLogger(CampaignWorker.class);
  
  @Value("${multi-instance-example.number-of-buckets}")
  public long numberOfBuckets;
  
  @Value("${multi-instance-example.number-of-elements}")
  public long numberOfElements;

  
  @Autowired
  ZeebeClient zeebeClient;

  @JobWorker
  public Map<String, Object> bucketCreation(JobClient client, ActivatedJob job) throws Exception {
    LOG.info("Handling bucketCreation");
    Map<String,Object> variables = new HashMap<>();
    
    List<String> bucketList = new ArrayList<String>();
    for (int i = 1; i < numberOfBuckets + 1; i++) {
      bucketList.add("bucket" + i);
    }
    variables.put("bucketList", bucketList);
    
    return variables;
  }
  
  @JobWorker
  public Map<String, Object> bucketStartSender(JobClient client, ActivatedJob job) {
    Map<String, Object> variables = job.getVariablesAsMap();
    LOG.info("Handling start bucket execution with vars{}", variables);
    String bucketId = (String) variables.get("bucketId");
    String businessKey = (String) variables.get("businessKey");
    LOG.info("Working on bucket {}", bucketId);
    PublishMessageResponse messageResponse = zeebeClient
        .newPublishMessageCommand()
        .messageName("StartBucketMessage")
        .correlationKey(bucketId)
        .variables(Map.of(
            "bucketId", bucketId,
            "businessKey", businessKey))
        .send()
        .join();
    return Map.of("MessageKey", messageResponse.getMessageKey());
  }
  
  @JobWorker
  public Map<String, Object> itemListCreation(JobClient client, ActivatedJob job) {
    Map<String, Object> variables = job.getVariablesAsMap();
    LOG.info("Get the elements from the bucket with vars {}", variables);
    Map<String,Object> returnVariables = new HashMap<>();
    ArrayList<String> letterList = new ArrayList<>();
    for (int i = 1; i < numberOfElements + 1; i++) {
      letterList.add("letter" + i);
    }
    returnVariables.put("itemList", letterList);
    return returnVariables;
  }
  
  @JobWorker
  public Map<String, Object> letterCreation(JobClient client, ActivatedJob job) {
    Map<String, Object> variables = job.getVariablesAsMap();
    LOG.info("Create a letter with variables {}", variables);
    String letterId = "letter-" + variables.get("itemId") + variables.get("bucketId");
    LOG.info("Letter with id {} created.", letterId);
    return Map.of("letterId", letterId);
  }
  
  @JobWorker
  public Map<String, Long> bucketResultSender(JobClient client, ActivatedJob job) {
    Map<String, Object> variables = job.getVariablesAsMap();
    LOG.info("send the bucket result with variables {}", variables);
    
    String bucketId = (String) variables.get("bucketId");
    PublishMessageResponse messageResponse = zeebeClient
        .newPublishMessageCommand()
        .messageName("bucketCompletedMessage")
        .correlationKey(bucketId)
        .send()
        .join();
    return Map.of("responseMessageKey", messageResponse.getMessageKey());
  }

}
