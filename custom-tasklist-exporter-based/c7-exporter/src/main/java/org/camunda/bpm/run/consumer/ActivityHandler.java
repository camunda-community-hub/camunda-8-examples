package org.camunda.bpm.run.consumer;

import static java.util.Collections.singletonList;

import com.camunda.consulting.BpmnErrorAction;
import com.camunda.consulting.CompleteAction;
import com.camunda.consulting.MessageAction;
import com.camunda.consulting.TaskHandler;
import com.camunda.consulting.impl.CompletedTaskMessage;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.camunda.bpm.engine.TaskService;

public class ActivityHandler implements TaskHandler, Runnable {

  private final AtomicBoolean shutdown = new AtomicBoolean(false);
  private final CountDownLatch shutdownLatch = new CountDownLatch(1);
  private final TaskService taskService;

  private final KafkaConsumer consumer;
  private final String sourceId;

  private final Logger LOGGER = Logger.getLogger(ActivityHandler.class.getName());

  public ActivityHandler(TaskService taskService, KafkaConsumer consumer, String sourceId) {
    this.taskService = taskService;
    this.consumer = consumer;
    this.sourceId = sourceId;
  }

  @Override
  public void run() {
    try {
      this.consumer.subscribe(singletonList(this.sourceId));
      while (!shutdown.get()) {
        ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(100));
        records.forEach(record -> {
          CompletedTaskMessage message = (CompletedTaskMessage) record.value();
          completeTask(message);
        });
      }
    } finally {
      consumer.close();
      shutdownLatch.countDown();
    }
  }

  @Override
  public void completeTask(CompleteAction completeAction) {
    try {
      taskService.complete(completeAction.getId(), completeAction.getVariables());
    } catch (Exception e) {
      LOGGER.severe("Error completing user task: " + completeAction.getId() + " " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  public void throwError(BpmnErrorAction bpmnErrorAction) {

  }

  @Override
  public void correlateMessage(MessageAction messageAction) {

  }
}
