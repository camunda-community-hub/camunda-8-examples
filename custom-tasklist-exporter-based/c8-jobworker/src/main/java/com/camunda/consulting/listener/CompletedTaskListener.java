package com.camunda.consulting.listener;

import com.camunda.consulting.CompleteAction;
import io.camunda.zeebe.client.ZeebeClient;
import java.util.logging.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CompletedTaskListener {

  private final Logger LOGGER = Logger.getLogger(CompletedTaskListener.class.getName());

  private final ZeebeClient client;

  public CompletedTaskListener(ZeebeClient client) {
    System.out.println("CompletedTaskListener Created");
    this.client = client;
  }


  @KafkaListener(topics = "${tasklist.kafka.topic}")
  public void listen(CompleteAction message) throws CannotCompleteTaskException {
    LOGGER.info("Received CompletedTaskMessage: " + message);
    try {
      client.newCompleteCommand(Long.parseLong(message.getId()))
          .variables(message.getVariables())
          .send()
          .join();
    } catch (Exception e) {
      throw new CannotCompleteTaskException("Error while completing UserTask: " + message + " - " + e.getMessage());
    }
  }

}
